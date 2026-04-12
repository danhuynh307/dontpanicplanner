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

    public WeeklyScheduleService(
            TaskService taskService,
            AvailabilityService availabilityService,
            PriorityScoreService priorityScoreService
    ) {
        this.taskService = taskService;
        this.availabilityService = availabilityService;
        this.priorityScoreService = priorityScoreService;
    }

    public WeeklyScheduleResponse generateWeeklySchedule(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);

        // Choose a stable planning anchor.
        // This keeps earlier weeks "consuming" tasks before later weeks are built.
        LocalDate planningStart = getPlanningStart(weekStart);

        List<Task> allTasks = taskService.getAllTasks();
        TaskDataStructure<Task> rankedTasks = new TaskDataStructure<>();

        for (Task task : allTasks) {
            rankedTasks.add(task);
        }

        TaskRankSystem.rankTasks(rankedTasks);

        List<Task> sessionTasks = new ArrayList<>();
        for (int i = 0; i < rankedTasks.size(); i++) {
            Task task = rankedTasks.get(i);
            sessionTasks.addAll(TaskSplitter.splitInto30MinSessions(task, priorityScoreService));
        }

        List<AvailabilityBlock> recurringAvailability = availabilityService.getAvailability();
        List<DatedAvailabilitySlot> datedSlots = buildDatedSlots(recurringAvailability, planningStart, weekEnd);

        List<ScheduledTaskBlock> allScheduledBlocks = placeSessionsAcrossSlots(sessionTasks, datedSlots);

        List<ScheduledTaskBlock> requestedWeekBlocks = new ArrayList<>();
        for (ScheduledTaskBlock block : allScheduledBlocks) {
            LocalDate blockDate = LocalDate.parse(block.getDate());
            if (!blockDate.isBefore(weekStart) && !blockDate.isAfter(weekEnd)) {
                requestedWeekBlocks.add(block);
            }
        }




        return new WeeklyScheduleResponse(
                weekStart.toString(),
                weekEnd.toString(),
                requestedWeekBlocks
        );
    }

    private LocalDate getPlanningStart(LocalDate requestedWeekStart) {
        LocalDate today = LocalDate.now();
        return today.minusDays(today.getDayOfWeek().getValue() % 7);
    }

    private List<DatedAvailabilitySlot> buildDatedSlots(
            List<AvailabilityBlock> recurringAvailability,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<DatedAvailabilitySlot> slots = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int actualDayOfWeek = toCalendarDayIndex(date);

            for (AvailabilityBlock block : recurringAvailability) {
                int blockDay = mapDayOfWeek(block.getDayOfWeek());
                if (blockDay == actualDayOfWeek) {
                    slots.add(new DatedAvailabilitySlot(
                            date.toString(),
                            actualDayOfWeek,
                            block.getStartTime(),
                            block.getEndTime()
                    ));
                }
            }
        }

        return slots;
    }

    private List<ScheduledTaskBlock> placeSessionsAcrossSlots(
            List<Task> sessionTasks,
            List<DatedAvailabilitySlot> slots
    ) {
        List<ScheduledTaskBlock> blocks = new ArrayList<>();
        long id = 1;
        int sessionIndex = 0;

        for (DatedAvailabilitySlot slot : slots) {
            LocalTime currentStart = LocalTime.parse(slot.getStartTime());
            LocalTime slotEnd = LocalTime.parse(slot.getEndTime());

            while (sessionIndex < sessionTasks.size()) {
                Task task = sessionTasks.get(sessionIndex);

                if (task.getEstimatedTime() == null || task.getEstimatedTime() <= 0) {
                    sessionIndex++;
                    continue;
                }

                long minutes = Math.round(task.getEstimatedTime() * 60);
                LocalTime currentEnd = currentStart.plusMinutes(minutes);

                if (currentEnd.isAfter(slotEnd)) {
                    break;
                }

                blocks.add(new ScheduledTaskBlock(
                        id++,
                        task.getName(),
                        slot.getDate(),
                        slot.getDayOfWeek(),
                        formatTime(currentStart),
                        formatTime(currentEnd),
                        getColorForPriority(task.getPriorityScore())
                ));

                currentStart = currentEnd;
                sessionIndex++;
            }

            if (sessionIndex >= sessionTasks.size()) {
                break;
            }
        }

        return blocks;
    }

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

    private int mapDayOfWeek(String day) {
        if (day == null) return 0;

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

    private String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

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