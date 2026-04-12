package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeeklyScheduleService {

    private final ScheduleGenerator scheduleGenerator;
    private final TaskService taskService;
    private final AvailabilityService availabilityService;

    public WeeklyScheduleService(ScheduleGenerator scheduleGenerator, TaskService taskService, AvailabilityService availabilityService) {
        this.scheduleGenerator = scheduleGenerator;
        this.taskService = taskService;
        this.availabilityService = availabilityService;
    }

    public WeeklyScheduleResponse generateWeeklySchedule(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);

        // Load real tasks and availability
        TaskDataStructure<Task> taskList = new TaskDataStructure<>();
        for (Task t : taskService.getAllTasks()) {
            taskList.add(t);
        }
        List<AvailabilityBlock> availabilityBlocks = availabilityService.getAvailability();

        // Generate scheduled groups
        List<ScheduledTaskGroup> scheduledGroups = scheduleGenerator.generateSchedule(taskList, availabilityBlocks);

        // Convert scheduler output into frontend response blocks
        List<ScheduledTaskBlock> blocks = convertToBlocks(scheduledGroups);

        return new WeeklyScheduleResponse(
                weekStart.toString(),
                weekEnd.toString(),
                blocks
        );
    }

    private List<ScheduledTaskBlock> convertToBlocks(List<ScheduledTaskGroup> groups) {
        List<ScheduledTaskBlock> blocks = new ArrayList<>();
        long id = 1;

        for (ScheduledTaskGroup group : groups) {
            if (group.getAvailabilityBlocks() == null || group.getAvailabilityBlocks().isEmpty()) {
                continue;
            }

            AvailabilityBlock availabilityBlock = group.getAvailabilityBlocks().get(0);

            int dayOfWeek = mapDayOfWeek(availabilityBlock.getDayOfWeek());
            java.time.LocalTime currentStart = java.time.LocalTime.parse(availabilityBlock.getStartTime());
            java.time.LocalTime blockEnd = java.time.LocalTime.parse(availabilityBlock.getEndTime());

            for (Task task : group.getTasks()) {
                if (task.getEstimatedTime() == null || task.getEstimatedTime() <= 0) {
                    continue;
                }

                long minutes = Math.round(task.getEstimatedTime() * 60);
                java.time.LocalTime currentEnd = currentStart.plusMinutes(minutes);

                if (currentEnd.isAfter(blockEnd)) {
                    break;
                }

                blocks.add(new ScheduledTaskBlock(
                        id++,
                        task.getName(),
                        dayOfWeek,
                        formatTime(currentStart),
                        formatTime(currentEnd),
                        getColorForPriority(task.getPriorityScore())
                ));

                currentStart = currentEnd;
            }
        }

        return blocks;
    }

    private String getColorForPriority(int priority) {
        if (priority >= 8) {
            return "#cf9b7d";
        } else if (priority >= 5) {
            return "#89b86d";
        } else {
            return "#7da9cf";
        }
    }

    private int mapDayOfWeek(String day) {
        switch (day.trim().toUpperCase()) {
            case "SUNDAY":
                return 0;
            case "MONDAY":
                return 1;
            case "TUESDAY":
                return 2;
            case "WEDNESDAY":
                return 3;
            case "THURSDAY":
                return 4;
            case "FRIDAY":
                return 5;
            case "SATURDAY":
                return 6;
            default:
                return 0;
        }
    }

    private String formatTime(java.time.LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}