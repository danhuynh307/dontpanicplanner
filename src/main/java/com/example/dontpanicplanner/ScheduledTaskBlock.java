package com.example.dontpanicplanner;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.Duration;

public class ScheduledTaskBlock{
    private List<AvailabilityBlock> availabilityBlocks;
    private List<Task> tasks;

    public ScheduledTaskBlock(List<AvailabilityBlock> availabilityBlocks, List<Task> tasks) {
        this.availabilityBlocks = availabilityBlocks;
        this.tasks = tasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<AvailabilityBlock> getAvailabilityBlocks() {
        return availabilityBlocks;
    }

    public List<ScheduledTaskBlock> generateSchedule(TaskDataStructure<Task> tasks,
            List<AvailabilityBlock> availabilityBlocks)
    {
        // Rank tasks based on priority
        TaskRankSystem.rankTasks(tasks);
        List<ScheduledTaskBlock> result = new ArrayList<>();
        int taskIndex = 0;

        // Fill each availability block if available
        for(AvailabilityBlock block : availabilityBlocks)
        {
            LocalTime start = LocalTime.parse(block.getStartTime());
            LocalTime end = LocalTime.parse(block.getEndTime());
            double remainingTime =
                    Duration.between(start, end).toMinutes() / 60.0;

            List<Task> scheduledTasks = new ArrayList<>();
            while (remainingTime > 0 && taskIndex < tasks.size())
            {
                Task current = tasks.get(taskIndex);
                double taskTime = current.getEstimatedTime();

                //if the task fits in fully
                if (taskTime <= remainingTime) {
                    scheduledTasks.add(current);
                    remainingTime -= taskTime;
                    taskIndex++;
                }
                else //if the task is too big then split
                {
                    Task splitTask = new Task(
                            current.getName(),
                            current.getTaskType(),
                            remainingTime,
                            current.getDueDate(),
                            current.getGradeWeight(),
                            current.getCurrentGrade()
                    );

                    scheduledTasks.add(splitTask);
                    current.setEstimatedTime(taskTime - remainingTime);
                    remainingTime = 0;
                }
            }

            result.add(new ScheduledTaskBlock(
                    List.of(block),
                    scheduledTasks
            ));
        }
        return result;
    }


}