package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

@Service
public class TaskRankSystem {

    private static PriorityScoreService scoreService = new PriorityScoreService();

    // Constructor injection (instead of new)
    public TaskRankSystem(PriorityScoreService scoreService) {
        this.scoreService = scoreService;
    }

    // calculates scores and sorts tasks by priority
    public static void rankTasks(TaskDataStructure<Task> tasks) {
        applyScores(tasks);
        sortByPriority(tasks);
    }

    // Compute scores for all tasks
    private static void applyScores(TaskDataStructure<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            scoreService.applyPriorityScore(t);
        }
    }

    // Sorts tasks
    private static void sortByPriority(TaskDataStructure<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.size() - 1; j++) {

                Task t1 = tasks.get(j);
                Task t2 = tasks.get(j + 1);

                int p1 = (t1.getPriorityScore() == null) ? 0 : t1.getPriorityScore();
                int p2 = (t2.getPriorityScore() == null) ? 0 : t2.getPriorityScore();

                if (p1 < p2) {
                    tasks.set(j, t2);
                    tasks.set(j + 1, t1);
                }
            }
        }
    }
}