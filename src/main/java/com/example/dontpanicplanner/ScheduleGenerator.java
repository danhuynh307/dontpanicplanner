package com.example.dontpanicplanner;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

// computes scheduling logic, places tasks inside availability blocks based on priority score
@Service
public class ScheduleGenerator {

    private final TaskRankSystem taskRankSystem;
    private final PriorityScoreService priorityScoreService;
    private static final double MAX_HOURS_PER_DAY = 3.0;

    public ScheduleGenerator(TaskRankSystem taskRankSystem, PriorityScoreService priorityScoreService) {
        this.taskRankSystem = taskRankSystem;
        this.priorityScoreService = priorityScoreService;
    }

    public List<ScheduledTaskGroup> generateSchedule(TaskDataStructure<Task> tasks, List<AvailabilityBlock> availabilityBlocks) {
        taskRankSystem.rankTasks(tasks);

        double totalWorkHours = 0;
        for (int i = 0; i < tasks.size(); i++) {
            totalWorkHours += tasks.get(i).getEstimatedTime();
        }

        Set<LocalDate> uniqueDays = new HashSet<>();
        for (AvailabilityBlock block : availabilityBlocks) {
            uniqueDays.add(block.getDate());
        }

        int numDays = uniqueDays.size();
        double targetPerDay = (numDays > 0) ? totalWorkHours / numDays : 0;
        double effectiveTargetPerDay = Math.max(targetPerDay, 0.5);

        Map<Long, List<Task>> sessionsByTask = new LinkedHashMap<>();
        Map<Long, Task> originalTask = new HashMap<>(); // need to read due date

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            Long taskId = t.getId();
            if (taskId == null) {
                continue;
            }
            originalTask.put(taskId, t);
            List<Task> splitSessions = TaskSplitter.splitInto30MinSessions(t, priorityScoreService);
            for (Task session : splitSessions) {
                session.setId(taskId);
            }

            sessionsByTask.put(taskId, new ArrayList<>(splitSessions));
        }


        List<ScheduledTaskGroup> result = new ArrayList<>();
        Map<LocalDate, Double> scheduledHoursByDay = new HashMap<>();

        // create one result group per block first
        for (AvailabilityBlock block : availabilityBlocks) {
            result.add(new ScheduledTaskGroup(List.of(block), new ArrayList<>()));
        }

        // balance as strictly as possible
        for (int b = 0; b < availabilityBlocks.size(); b++) {
            AvailabilityBlock block = availabilityBlocks.get(b);
            List<Task> scheduledInBlock = result.get(b).getTasks();

            double blockCapacity = java.time.Duration.between(
                    java.time.LocalTime.parse(block.getStartTime()),
                    java.time.LocalTime.parse(block.getEndTime())
            ).toMinutes() / 60.0;

            // subtract anything already placed in this block
            for (Task task : scheduledInBlock) {
                blockCapacity -= task.getEstimatedTime();
            }

            LocalDate blockDate = block.getDate();
            double currentDayWork = scheduledHoursByDay.getOrDefault(blockDate, 0.0);

            for (Long taskId : sessionsByTask.keySet()) {
                List<Task> sessions = sessionsByTask.get(taskId);
                Task origTask = originalTask.get(taskId);
                LocalDate dueDate = origTask.getDueDate();

                if (blockDate.isAfter(dueDate)) {
                    continue;
                }

                while (!sessions.isEmpty()) {
                    Task session = sessions.get(0);
                    double sessionTime = session.getEstimatedTime();

                    boolean underTarget = (currentDayWork + sessionTime) <= effectiveTargetPerDay;
                    boolean fitsInBlock = sessionTime <= blockCapacity;

                    if (underTarget && fitsInBlock) {
                        scheduledInBlock.add(sessions.remove(0));
                        currentDayWork += sessionTime;
                        scheduledHoursByDay.put(blockDate, currentDayWork);
                        blockCapacity -= sessionTime;
                    } else {
                        break;
                    }
                }
            }
        }

        // allow overflow AFTER balancing
        for (int b = 0; b < availabilityBlocks.size(); b++) {
            AvailabilityBlock block = availabilityBlocks.get(b);
            List<Task> scheduledInBlock = result.get(b).getTasks();

            double blockCapacity = java.time.Duration.between(
                    java.time.LocalTime.parse(block.getStartTime()),
                    java.time.LocalTime.parse(block.getEndTime())
            ).toMinutes() / 60.0;

            for (Task task : scheduledInBlock) {
                blockCapacity -= task.getEstimatedTime();
            }

            LocalDate blockDate = block.getDate();
            double currentDayWork = scheduledHoursByDay.getOrDefault(blockDate, 0.0);

            for (Long taskId : sessionsByTask.keySet()) {
                List<Task> sessions = sessionsByTask.get(taskId);
                Task origTask = originalTask.get(taskId);
                LocalDate dueDate = origTask.getDueDate();

                if (blockDate.isAfter(dueDate)) {
                    continue;
                }

                while (!sessions.isEmpty()) {
                    Task session = sessions.get(0);
                    double sessionTime = session.getEstimatedTime();

                    boolean fitsInBlock = sessionTime <= blockCapacity;

                    double remainingTaskHours = getRemainingTaskHours(sessions);
                    double futureCapacityBeforeDue = getFutureCapacityBeforeDue(availabilityBlocks, b, dueDate, scheduledHoursByDay, effectiveTargetPerDay);

                    // only overflow now if waiting would leave too little room later
                    boolean mustOverflowNow = futureCapacityBeforeDue < remainingTaskHours;

                    if (fitsInBlock && mustOverflowNow) {
                        scheduledInBlock.add(sessions.remove(0));
                        currentDayWork += sessionTime;
                        scheduledHoursByDay.put(blockDate, currentDayWork);
                        blockCapacity -= sessionTime;
                    } else {
                        break;
                    }
                }
            }
        }
        return result;
    }

 /**
 * Returns all tasks that could NOT be scheduled into any availability block.
 * These are reported back to the user so they know which tasks need attention.
 * AR
 *
 */
public List<Task> getUnscheduledTasks(
        TaskDataStructure<Task> tasks,
        List<ScheduledTaskGroup> scheduledGroups)
{
    // Collect the names of all tasks that made it into the schedule
    Map<Long, Double> scheduledHoursByTask = new HashMap<>();

    for (ScheduledTaskGroup group : scheduledGroups) {
        for (Task scheduledTask : group.getTasks()) {
            // Strip the "(Part X/Y)" suffix to get the original task name
            Long taskId = scheduledTask.getId();
            if (taskId == null) {
                continue;
            }
            scheduledHoursByTask.put(taskId, scheduledHoursByTask.getOrDefault(taskId, 0.0) + scheduledTask.getEstimatedTime());
        }
    }

    // Find tasks from the full list that are NOT in the schedule
    List<Task> unscheduled = new ArrayList<>();

    for (int i = 0; i < tasks.size(); i++) {
        Task task = tasks.get(i);
        double scheduledHours = scheduledHoursByTask.getOrDefault(task.getName(), 0.0);
        if (scheduledHours + 0.0001 < task.getEstimatedTime()) { // small val to see if task is partially allocated
            unscheduled.add(task);
        }
    }

    return unscheduled;
    }

    private double getFutureCapacityBeforeDue(
            List<AvailabilityBlock> availabilityBlocks,
            int currentBlockIndex,
            LocalDate dueDate,
            Map<LocalDate, Double> scheduledHoursByDay,
            double effectiveTargetPerDay) {

        double futureCapacity = 0.0;

        for (int i = currentBlockIndex + 1; i < availabilityBlocks.size(); i++) {
            AvailabilityBlock futureBlock = availabilityBlocks.get(i);
            LocalDate futureDate = futureBlock.getDate();

            if (futureDate.isAfter(dueDate)) {
                continue;
            }

            double blockCapacity = java.time.Duration.between(
                    java.time.LocalTime.parse(futureBlock.getStartTime()),
                    java.time.LocalTime.parse(futureBlock.getEndTime())
            ).toMinutes() / 60.0;

            double alreadyScheduledThatDay = scheduledHoursByDay.getOrDefault(futureDate, 0.0);
            double remainingBalancedRoom = Math.max(0.0, effectiveTargetPerDay - alreadyScheduledThatDay);

            futureCapacity += Math.min(blockCapacity, remainingBalancedRoom);
        }

        return futureCapacity;
    }

    private double getRemainingTaskHours(List<Task> sessions) {
        double total = 0.0;
        for (Task session : sessions) {
            total += session.getEstimatedTime();
        }
        return total;
    }








}


