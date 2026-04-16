package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeeklyScheduleService {

    private final TaskService taskService;
    private final AvailabilityService availabilityService;
    private final PriorityScoreService priorityScoreService;
    private final TaskRankSystem taskRankSystem;
    private final ScheduleGenerator scheduleGenerator;

    // break is inserted after 2 hours of consecutive work
    private static final double BREAK_TRIGGER_HOURS = 2.0;
    // break length is 30 minutes
    private static final double BREAK_DURATION_MINUTES = 30;

    public WeeklyScheduleService(TaskService taskService, AvailabilityService availabilityService,
                                  PriorityScoreService priorityScoreService, TaskRankSystem taskRankSystem,
                                  ScheduleGenerator scheduleGenerator) {
        this.taskService = taskService;
        this.availabilityService = availabilityService;
        this.priorityScoreService = priorityScoreService;
        this.taskRankSystem = taskRankSystem;
        this.scheduleGenerator = scheduleGenerator;
    }

    // generates weekly schedule using schedule generator
    public WeeklyScheduleResponse generateWeeklySchedule(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate planningStart = getPlanningStart(weekStart);

        TaskDataStructure<Task> taskStore = new TaskDataStructure<>();
        for (Task task : taskService.getAllTasks()) {
            taskStore.add(task);
        }

        List<AvailabilityBlock> recurringAvailability = availabilityService.getAvailability();
        List<AvailabilityBlock> weekAvailability = buildWeekAvailability(recurringAvailability, planningStart, weekEnd);
        List<ScheduledTaskGroup> scheduledGroups = scheduleGenerator.generateSchedule(taskStore, weekAvailability);
        List<ScheduledTaskBlock> blocks = convertGroupsToBlocks(scheduledGroups, weekStart);
        return new WeeklyScheduleResponse(weekStart.toString(), weekEnd.toString(), blocks);
    }

    // get starting date from local time
    private LocalDate getPlanningStart(LocalDate requestedWeekStart) {
        LocalDate today = LocalDate.now();
        LocalDate startOfCurrentWeek = today.minusDays(today.getDayOfWeek().getValue() % 7);

        LocalDate baseStart = requestedWeekStart.isBefore(startOfCurrentWeek) ? startOfCurrentWeek : requestedWeekStart;
        return baseStart.isBefore(today) ? today : baseStart;
    }

    // helper that builds the blocks needed
    private List<AvailabilityBlock> buildWeekAvailability(List<AvailabilityBlock> recurringAvailability,
                                                           LocalDate weekStart, LocalDate weekEnd) {
        List<AvailabilityBlock> result = new ArrayList<>();

        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            String dayName = date.getDayOfWeek().name();

            for (AvailabilityBlock block : recurringAvailability) {
                if (block.getDayOfWeek() != null &&
                        block.getDayOfWeek().trim().equalsIgnoreCase(dayName)) {

                    AvailabilityBlock datedBlock = new AvailabilityBlock();
                    datedBlock.setDayOfWeek(block.getDayOfWeek());
                    datedBlock.setStartTime(block.getStartTime());
                    datedBlock.setEndTime(block.getEndTime());
                    datedBlock.setDate(date);

                    result.add(datedBlock);
                }
            }
        }

        return result;
    }

    /**
     * Converts task groups into display blocks for the frontend.
     * Inserts 30-minute breaks after 2 hours of consecutive work FIRST,
     * then recombines consecutive split sessions of the same task.
     *
     * By AR
     */
    private List<ScheduledTaskBlock> convertGroupsToBlocks(List<ScheduledTaskGroup> groups, LocalDate weekStart) {
        List<ScheduledTaskBlock> blocks = new ArrayList<>();
        long id = 1;

        for (int i = 0; i < groups.size(); i++) {
            ScheduledTaskGroup group = groups.get(i);

            if (group.getAvailabilityBlocks().isEmpty()) {
                continue;
            }

            AvailabilityBlock block = group.getAvailabilityBlocks().get(0);
            LocalDate date = block.getDate();
            LocalTime currentStart = LocalTime.parse(block.getStartTime());

            // Step 1: Insert breaks FIRST (before recombining)
            List<Object> withBreaks = insertBreaks(group.getTasks());

            // Step 2: Recombine consecutive split sessions of the same task
            List<Object> finalItems = recombineBlocks(withBreaks);

            // Step 3: Convert to ScheduledTaskBlock for frontend
            for (Object item : finalItems) {
                if (item instanceof Task task) {
                    long minutes = Math.round(task.getEstimatedTime() * 60);
                    LocalTime currentEnd = currentStart.plusMinutes(minutes);

                    blocks.add(new ScheduledTaskBlock(
                            id++,
                            task.getName(),
                            date.toString(),
                            toCalendarDayIndex(date),
                            formatTime(currentStart),
                            formatTime(currentEnd),
                            getColorForPriority(task.getPriorityScore())
                    ));

                    currentStart = currentEnd;

                } else if (item instanceof String && item.equals("BREAK")) {
                    // Insert a 30-minute break block
                    LocalTime breakEnd = currentStart.plusMinutes((long) BREAK_DURATION_MINUTES);

                    blocks.add(new ScheduledTaskBlock(
                            id++,
                            "Break",
                            date.toString(),
                            toCalendarDayIndex(date),
                            formatTime(currentStart),
                            formatTime(breakEnd),
                            "#A8D5A2"  // lightGreenColor for breaks
                    ));

                    currentStart = breakEnd;
                }
            }
        }

        return blocks;
    }

    /**
     * Recombines consecutive split sessions of the same task into one larger block.
     * Works on a mixed list of Task and "BREAK" markers.
     * Recomputes priority score on the merged task.
     *
     * Example: "Essay (Part 1/4)" + "Essay (Part 2/4)" → "Essay" with 1.0h
     *
     * by AR
     */
    private List<Object> recombineBlocks(List<Object> items) {
        List<Object> result = new ArrayList<>();

        int i = 0;
        while (i < items.size()) {
            Object current = items.get(i);

            // Keep breaks as-is
            if (current instanceof String) {
                result.add(current);
                i++;
                continue;
            }

            Task currentTask = (Task) current;
            String baseName = getBaseName(currentTask.getName());
            double combinedTime = currentTask.getEstimatedTime();
            int j = i + 1;

            // Look ahead for consecutive sessions of the same task (stop at breaks)
            while (j < items.size() && items.get(j) instanceof Task) {
                Task next = (Task) items.get(j);
                String nextBase = getBaseName(next.getName());

                if (nextBase.equals(baseName)) {
                    combinedTime += next.getEstimatedTime();
                    j++;
                } else {
                    break;
                }
            }

            if (j > i + 1) {
                // Multiple consecutive sessions — merge them
                Task merged = new Task(
                        baseName,
                        currentTask.getTaskType(),
                        combinedTime,
                        currentTask.getDueDate(),
                        currentTask.getGradeWeight(),
                        currentTask.getCurrentGrade()
                );
                // Recompute priority score for the merged block
                priorityScoreService.applyPriorityScore(merged);
                result.add(merged);
            } else {
                // Single session — keep as is but strip "(Part X/Y)" from name
                Task cleaned = new Task(
                        baseName,
                        currentTask.getTaskType(),
                        currentTask.getEstimatedTime(),
                        currentTask.getDueDate(),
                        currentTask.getGradeWeight(),
                        currentTask.getCurrentGrade()
                );
                priorityScoreService.applyPriorityScore(cleaned);
                result.add(cleaned);
            }

            i = j;
        }

        return result;
    }

    /**
     * Inserts a 30-minute break after every 2 hours of consecutive work.
     * Only adds a break if there are more tasks after it that day.
     * Returns a mixed list of Task and "BREAK" string markers.
     *
     * by AR
     */
    private List<Object> insertBreaks(List<Task> tasks) {
        List<Object> result = new ArrayList<>();
        double consecutiveHours = 0.0;

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            result.add(task);
            consecutiveHours += task.getEstimatedTime();

            // Only add break if threshold hit AND more tasks follow
            if (consecutiveHours >= BREAK_TRIGGER_HOURS && i < tasks.size() - 1) {
                result.add("BREAK");
                consecutiveHours = 0.0;  // reset counter after break
            }
        }

        return result;
    }

    /**
     * Strips the "(Part X/Y)" suffix from a split task name to get the original name.
     */
    private String getBaseName(String name) {
        if (name != null && name.contains(" (Part ")) {
            return name.substring(0, name.indexOf(" (Part "));
        }
        return name;
    }

    // gets day of week index from local date
    private int toCalendarDayIndex(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SUNDAY -> 0;
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY -> 4;
            case FRIDAY -> 5;
            case SATURDAY -> 6;
        };
    }

    // converts local time to HH:mm string
    private String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    // returns color based on priority score
    private String getColorForPriority(double priority) {
        if (priority >= 8) {
            return "#cf9b7d";
        } else if (priority >= 5) {
            return "#89b86d";
        } else {
            return "#7da9cf";
        }
    }
}
