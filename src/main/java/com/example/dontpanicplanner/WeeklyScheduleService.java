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

        List<Task> allTasks = taskService.getAllTasks();

        TaskDataStructure<Task> taskStore = new TaskDataStructure<>();
        for (Task task : allTasks) {
            taskStore.add(task);
        }

        // always plan from today so every call produces the same placement —
        // tasks fill the earliest available slots and later weeks only show
        // whatever the scheduler couldn't fit in earlier weeks
        LocalDate planningStart = LocalDate.now();
        LocalDate furthestDue   = getFurthestDueDate(allTasks);
        LocalDate planningEnd   = furthestDue != null ? furthestDue : weekEnd;

        List<AvailabilityBlock> recurringAvailability = availabilityService.getAvailability();
        List<AvailabilityBlock> fullAvailability = buildWeekAvailability(recurringAvailability, planningStart, planningEnd);

        List<ScheduledTaskGroup> scheduledGroups = scheduleGenerator.generateSchedule(taskStore, fullAvailability);

        // filter to blocks that fall inside the requested week only
        List<ScheduledTaskBlock> blocks = convertGroupsToBlocks(scheduledGroups, weekStart, weekEnd);
        return new WeeklyScheduleResponse(weekStart.toString(), weekEnd.toString(), blocks);
    }

    // returns the latest due date across all tasks, or null if there are none
    private LocalDate getFurthestDueDate(List<Task> tasks) {
        LocalDate furthest = null;
        for (Task task : tasks) {
            if (task.getDueDate() != null) {
                if (furthest == null || task.getDueDate().isAfter(furthest)) {
                    furthest = task.getDueDate();
                }
            }
        }
        return furthest;
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
     * Only includes groups whose date falls within weekStart–weekEnd.
     * Order: recombineBlocks FIRST, then insertBreaks.
     * This ensures split sessions are merged before breaks are placed.
     *
     * By AR
     */
    private List<ScheduledTaskBlock> convertGroupsToBlocks(List<ScheduledTaskGroup> groups, LocalDate weekStart, LocalDate weekEnd) {
        List<ScheduledTaskBlock> blocks = new ArrayList<>();
        long id = 1;

        for (int i = 0; i < groups.size(); i++) {
            ScheduledTaskGroup group = groups.get(i);

            if (group.getAvailabilityBlocks().isEmpty()) {
                continue;
            }

            AvailabilityBlock block = group.getAvailabilityBlocks().get(0);
            LocalDate date = block.getDate();

            // skip any group that falls outside the week being viewed
            if (date.isBefore(weekStart) || date.isAfter(weekEnd)) {
                continue;
            }

            LocalTime currentStart = LocalTime.parse(block.getStartTime());

            // Step 1: Recombine consecutive split sessions FIRST
            List<Task> recombined = recombineBlocks(group.getTasks());

            // Step 2: Insert breaks AFTER recombining
            List<Object> finalItems = insertBreaks(recombined);

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
     * Recomputes priority score on the merged task.
     *
     * Example: "Essay (Part 1/4)" + "Essay (Part 2/4)" → "Essay" with 1.0h
     *
     * by AR
     */
    private List<Task> recombineBlocks(List<Task> tasks) {
        List<Task> result = new ArrayList<>();

        int i = 0;
        while (i < tasks.size()) {
            Task current = tasks.get(i);
            String baseName = getBaseName(current.getName());
            double combinedTime = current.getEstimatedTime();
            int j = i + 1;

            // Look ahead for consecutive sessions of the same task
            while (j < tasks.size()) {
                Task next = tasks.get(j);
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
                        current.getTaskType(),
                        combinedTime,
                        current.getDueDate(),
                        current.getGradeWeight(),
                        current.getCurrentGrade()
                );
                // Recompute priority score for the merged block
                priorityScoreService.applyPriorityScore(merged);
                result.add(merged);
            } else {
                // Single session — keep as is but strip "(Part X/Y)" from name
                Task cleaned = new Task(
                        baseName,
                        current.getTaskType(),
                        current.getEstimatedTime(),
                        current.getDueDate(),
                        current.getGradeWeight(),
                        current.getCurrentGrade()
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
 * Also splits large single tasks that exceed 2 hours with breaks in between.
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
        double remaining = task.getEstimatedTime();
        boolean hasMoreTasksAfter = i < tasks.size() - 1;

        // If this single task exceeds the trigger, split it with breaks inside
        while (remaining > BREAK_TRIGGER_HOURS) {
            // Add a 2-hour chunk of this task
            Task chunk = new Task(
                    task.getName(),
                    task.getTaskType(),
                    BREAK_TRIGGER_HOURS,
                    task.getDueDate(),
                    task.getGradeWeight(),
                    task.getCurrentGrade()
            );
            priorityScoreService.applyPriorityScore(chunk);
            result.add(chunk);
            remaining -= BREAK_TRIGGER_HOURS;

            // Add break after the chunk if there's still work left
            if (remaining > 0 || hasMoreTasksAfter) {
                result.add("BREAK");
            }
            consecutiveHours = 0.0;
        }

        // Add the remaining portion of the task
        if (remaining > 0) {
            Task remainder = new Task(
                    task.getName(),
                    task.getTaskType(),
                    remaining,
                    task.getDueDate(),
                    task.getGradeWeight(),
                    task.getCurrentGrade()
            );
            priorityScoreService.applyPriorityScore(remainder);
            result.add(remainder);
            consecutiveHours += remaining;
        }

        // Check if consecutive hours across tasks triggers a break
        if (consecutiveHours >= BREAK_TRIGGER_HOURS && hasMoreTasksAfter) {
            result.add("BREAK");
            consecutiveHours = 0.0;
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
