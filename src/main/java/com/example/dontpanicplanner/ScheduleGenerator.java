package com.example.dontpanicplanner;
import org.springframework.stereotype.Service;

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

        int numDays = availabilityBlocks.size();
        double targetPerDay = (numDays > 0) ? totalWorkHours / numDays : 0;

        Map<String, List<Task>> sessionsByTask = new LinkedHashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            List<Task> splitSessions = TaskSplitter.splitInto30MinSessions(t,priorityScoreService);
            sessionsByTask.put(t.getName(), new ArrayList<>(splitSessions));
        }

        List<ScheduledTaskGroup> result = new ArrayList<>();
        for (AvailabilityBlock block : availabilityBlocks) {
            double blockCapacity = java.time.Duration.between(
                    java.time.LocalTime.parse(block.getStartTime()),
                    java.time.LocalTime.parse(block.getEndTime())
            ).toMinutes() / 60.0;

            List<Task> scheduledInBlock = new ArrayList<>();
            double currentDayWork = 0;
            for (String taskName : sessionsByTask.keySet()) {
                List<Task> sessions = sessionsByTask.get(taskName);

                while (!sessions.isEmpty()) {
                    Task session = sessions.get(0);
                    double sessionTime = session.getEstimatedTime();

                    boolean underTarget = (currentDayWork + sessionTime) <= targetPerDay;
                    boolean fitsInBlock = sessionTime <= blockCapacity;
                    boolean underMaxLimit = (currentDayWork + sessionTime) <= MAX_HOURS_PER_DAY;

                    if (underTarget && fitsInBlock && underMaxLimit) {
                        scheduledInBlock.add(sessions.remove(0));
                        currentDayWork += sessionTime;
                        blockCapacity -= sessionTime;
                    } else {
                        break;
                    }
                }
            }
            result.add(new ScheduledTaskGroup(List.of(block), scheduledInBlock));
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
    Set<String> scheduledTaskNames = new HashSet<>();

    for (ScheduledTaskGroup group : scheduledGroups) {
        for (Task scheduledTask : group.getTasks()) {
            // Strip the "(Part X/Y)" suffix to get the original task name
            String name = scheduledTask.getName();
            if (name.contains(" (Part ")) {
                name = name.substring(0, name.indexOf(" (Part "));
            }
            scheduledTaskNames.add(name);
        }
    }

    // Find tasks from the full list that are NOT in the schedule
    List<Task> unscheduled = new ArrayList<>();

    for (int i = 0; i < tasks.size(); i++) {
        Task task = tasks.get(i);
        if (!scheduledTaskNames.contains(task.getName())) {
            unscheduled.add(task);
        }
    }

    return unscheduled;
    }
}
