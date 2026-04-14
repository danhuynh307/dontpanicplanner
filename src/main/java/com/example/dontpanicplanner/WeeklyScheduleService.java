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

    public WeeklyScheduleService(TaskService taskService, AvailabilityService availabilityService, PriorityScoreService priorityScoreService, TaskRankSystem taskRankSystem, ScheduleGenerator scheduleGenerator) {
        this.taskService = taskService;
        this.availabilityService = availabilityService;
        this.priorityScoreService = priorityScoreService;
        this.taskRankSystem = taskRankSystem;
        this.scheduleGenerator = scheduleGenerator;
    }

    // generates weekly schedule using schedule generator. sets the week so that tasks do not persist over the next week.
    public WeeklyScheduleResponse generateWeeklySchedule(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate planningStart = getPlanningStart(weekStart);

        TaskDataStructure<Task> taskStore = new TaskDataStructure<>();
        for (Task task : taskService.getAllTasks()) {
            taskStore.add(task);
        }

        List<AvailabilityBlock> recurringAvailability = availabilityService.getAvailability();
        List<AvailabilityBlock> weekAvailability = buildWeekAvailability(recurringAvailability, weekStart, weekEnd);
        List<ScheduledTaskGroup> scheduledGroups = scheduleGenerator.generateSchedule(taskStore, weekAvailability);
        List<ScheduledTaskBlock> blocks = convertGroupsToBlocks(scheduledGroups, weekStart);
        return new WeeklyScheduleResponse(weekStart.toString(), weekEnd.toString(), blocks);
    }

    // get starting date from local time
    private LocalDate getPlanningStart(LocalDate requestedWeekStart) {
        LocalDate today = LocalDate.now();
        return today.minusDays(today.getDayOfWeek().getValue() % 7);
    }

    // helper that builds the blocks needed
    private List<AvailabilityBlock> buildWeekAvailability(List<AvailabilityBlock> recurringAvailability, LocalDate weekStart, LocalDate weekEnd) {
        List<AvailabilityBlock> result = new ArrayList<>();

        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            String dayName = date.getDayOfWeek().name();

            for (AvailabilityBlock block : recurringAvailability) {
                if (block.getDayOfWeek() != null &&
                        block.getDayOfWeek().trim().equalsIgnoreCase(dayName)) {
                    result.add(block);
                }
            }
        }

        return result;
    }

    // converts task groups into blocks (storing the week of the task as well)
    private List<ScheduledTaskBlock> convertGroupsToBlocks(List<ScheduledTaskGroup> groups, LocalDate weekStart) {
        List<ScheduledTaskBlock> blocks = new ArrayList<>();
        long id = 1;

        for (int i = 0; i < groups.size(); i++) {
            ScheduledTaskGroup group = groups.get(i);

            if (group.getAvailabilityBlocks().isEmpty()) {
                continue;
            }

            AvailabilityBlock block = group.getAvailabilityBlocks().get(0);
            LocalDate date = weekStart.plusDays(i);
            LocalTime currentStart = LocalTime.parse(block.getStartTime());

            for (Task task : group.getTasks()) {
                long minutes = Math.round(task.getEstimatedTime() * 60);
                LocalTime currentEnd = currentStart.plusMinutes(minutes);

                blocks.add(new ScheduledTaskBlock(id++, task.getName(), date.toString(), toCalendarDayIndex(date), formatTime(currentStart), formatTime(currentEnd), getColorForPriority(task.getPriorityScore())));

                currentStart = currentEnd;
            }
        }

        return blocks;
    }

    // gets day of week from local time
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
    // gets day of week given a string
    private int mapDayOfWeek(String day) {
        if (day == null) {
            return 0;
        }
        return switch (day.trim().toUpperCase()) {
            case "SUNDAY" -> 0;
            case "MONDAY" -> 1;
            case "TUESDAY" -> 2;
            case "WEDNESDAY" -> 3;
            case "THURSDAY" -> 4;
            case "FRIDAY" -> 5;
            case "SATURDAY" -> 6;
            default -> 0;
        };
    }
    // converts local time to string
    private String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
    // priority colors
    private String getColorForPriority(double priority) {
        if (priority >= 8) {
            return "#cf9b7d";
        }
        else if (priority >= 5) {
            return "#89b86d";
        }
        else {
            return "#7da9cf";
        }
    }
}