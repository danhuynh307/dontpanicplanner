package com.example.dontpanicplanner;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;


// computes scheduling logic, places tasks inside availability blocks based on priority score
@Service
public class ScheduleGenerator {

    public List<ScheduledTaskGroup> generateSchedule(TaskDataStructure<Task> tasks, List<AvailabilityBlock> availabilityBlocks) {
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
