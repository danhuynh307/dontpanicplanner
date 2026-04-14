package com.example.dontpanicplanner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskTest {
    public static void main(String[] args) {
        // 1. ENGINE SETUP
        PriorityScoreService scoreService = new PriorityScoreService();
        TaskRankSystem rankSystem = new TaskRankSystem(scoreService);
        ScheduleGenerator generator = new ScheduleGenerator(rankSystem, scoreService);

        // 2. AVAILABILITY: 6 Days (Monday through Saturday)
        // Each day has plenty of room (5 hours), so the 'target' will be the limiting factor.
        List<AvailabilityBlock> availability = new ArrayList<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (String day : days) {
            availability.add(new AvailabilityBlock(day, "09:00", "14:00"));
        }

        // 3. TASKS: Two 6-hour tasks (12 hours total)
        // 12 hours / 6 days = 2.0 hours per day target.
        TaskDataStructure<Task> taskStore = new TaskDataStructure<>();

        // Task A: Due Thursday (4 days from Monday)
        taskStore.add(new Task("CS Project", "Project", 6.0,
                LocalDate.now().plusDays(3), 30, 85.0));

        // Task B: Due Saturday (6 days from Monday)
        taskStore.add(new Task("Math Assignment", "HW", 6.0,
                LocalDate.now().plusDays(5), 20, 90.0));

        // 4. GENERATE
        List<ScheduledTaskGroup> schedule = generator.generateSchedule(taskStore, availability);

        // 5. OUTPUT RESULTS
        System.out.println("=== LOAD-BALANCED SCHEDULING TEST ===");
        System.out.println("Goal: 12h Work / 6 Days = 2.0h per day\n");

        for (ScheduledTaskGroup group : schedule) {
            AvailabilityBlock block = group.getAvailabilityBlocks().get(0);
            double dailyTotal = 0;

            System.out.println("📅 " + block.getDayOfWeek() + ":");

            if (group.getTasks().isEmpty()) {
                System.out.println("   (Empty)");
            } else {
                for (Task t : group.getTasks()) {
                    System.out.println("   - " + t.getName() + " (0.5h)");
                    dailyTotal += 0.5;
                }
            }
            System.out.println("   [Day Total: " + dailyTotal + "h]\n");
        }
    }

}
