package com.example.dontpanicplanner;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleGenerator {

    public List<ScheduledTaskBlock> generateSchedule(
            TaskDataStructure<Task> tasks,
            List<AvailabilityBlock> availabilityBlocks) {

        TaskRankSystem.rankTasks(tasks);

        List<Task> sessionTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            List<Task> splitSessions = TaskSplitter.splitInto30MinSessions(t, new PriorityScoreService());
            sessionTasks.addAll(splitSessions);
        }

        List<ScheduledTaskBlock> result = new ArrayList<>();
        int sessionIndex = 0;

        for (AvailabilityBlock block : availabilityBlocks) {
            LocalTime currentStart = LocalTime.parse(block.getStartTime());
            LocalTime blockEnd = LocalTime.parse(block.getEndTime());

            while (sessionIndex < sessionTasks.size() && currentStart.isBefore(blockEnd)) {
                Task currentTask = sessionTasks.get(sessionIndex);

                long sessionMinutes = (long) (currentTask.getEstimatedTime() * 60);
                LocalTime sessionEnd = currentStart.plusMinutes(sessionMinutes);

                if (sessionEnd.isAfter(blockEnd)) {
                    break;
                }

                result.add(new ScheduledTaskBlock(
                        currentTask.getId(),
                        currentTask.getTitle(),
                        block.getDayOfWeek(),
                        currentStart.toString(),
                        sessionEnd.toString(),
                        currentTask.getColor()
                ));

                currentStart = sessionEnd;
                sessionIndex++;
            }
        }

        return result;
    }
}