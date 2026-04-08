package com.example.dontpanicplanner;

import java.util.ArrayList;
import java.util.List;

public class ScheduleGenerator {

    public List<ScheduledTaskGroup> generateSchedule(
            TaskDataStructure<Task> tasks,
            List<AvailabilityBlock> availabilityBlocks)
    {
        TaskRankSystem.rankTasks(tasks);

        List<Task> sessionTasks = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            List<Task> splitSessions = TaskSplitter.splitInto30MinSessions(t, new PriorityScoreService());
            sessionTasks.addAll(splitSessions);
        }

        List<ScheduledTaskGroup> result = new ArrayList<>();
        int sessionIndex = 0;

        for (AvailabilityBlock block : availabilityBlocks) {
            double remainingTime =
                    java.time.Duration.between(
                            java.time.LocalTime.parse(block.getStartTime()),
                            java.time.LocalTime.parse(block.getEndTime())
                    ).toMinutes() / 60.0;

            List<Task> scheduled = new ArrayList<>();

            while (remainingTime > 0 && sessionIndex < sessionTasks.size()) {
                Task current = sessionTasks.get(sessionIndex);
                double sessionTime = current.getEstimatedTime();

                if (sessionTime <= remainingTime) {
                    scheduled.add(current);
                    remainingTime -= sessionTime;
                    sessionIndex++;
                } else {
                    break;
                }
            }

            result.add(new ScheduledTaskGroup(List.of(block), scheduled));
        }

        return result;
    }
}