package com.example.dontpanicplanner;

import java.time.LocalDate;

public class TaskTest {
    public static void main(String[] args) {

        TaskDataStructure<Task> tasks = new TaskDataStructure<>();
        Task t1 = new Task("Homework", "HW", 2.0,
                LocalDate.now().plusDays(5), 10, 85.0);
        Task t2 = new Task("Final Exam", "Exam", 3.0,
                LocalDate.now().plusDays(1), 25, 70.0);
        Task t3 = new Task("Quiz", "Quiz", 1.0,
                LocalDate.now().plusDays(7), 5, 95.0);
        Task t4 = new Task("Project", "Project", 4.0,
                LocalDate.now().plusDays(2), 20, 60.0);

        tasks.add(t1);
        tasks.add(t2);
        tasks.add(t3);
        tasks.add(t4);

        PriorityScoreService scoreService = new PriorityScoreService();
        TaskRankSystem rankSystem = new TaskRankSystem(scoreService);
        rankSystem.rankTasks(tasks);
        System.out.println("=== Ranked Tasks (Highest Priority First) ===");

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            System.out.println(t.getName() + " | Score: " + t.getPriorityScore() + " | Due: " + t.getDueDate());
        }

    }
}
