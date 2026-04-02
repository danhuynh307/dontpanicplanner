package com.example.dontpanicplanner;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskSplitter breaks a large task into smaller recommended work sessions.
 *
 * Rules:
 *  - Tasks under 0.5 hours (30 min) are NOT split — too short to divide.
 *  - Caller specifies how many sessions to split into.
 *  - Each session gets an equal share of the estimated time (in 30-min increments).
 *  - Priority score is recomputed on each session after splitting.
 *
 * Sprint 2 - Abdur Rahman
 */
public class TaskSplitter {

    // Minimum task length to be eligible for splitting (30 minutes = 0.5 hours)
    private static final double MIN_SPLITTABLE_HOURS = 0.5;

    // Minimum session length — each session must be at least 30 minutes
    private static final double MIN_SESSION_HOURS = 0.5;

    /**
     * Splits a task into a specified number of equal sessions.
     *
     * If the task is under 30 minutes, or if splitting would make each
     * session under 30 minutes, the original task is returned unchanged.
     *
     * Each session inherits the original task's due date, grade weight,
     * task type, and current grade. Priority score is recomputed on each
     * session using the provided PriorityScoreService.
     *
     * @param task           the task to split
     * @param numSessions    how many sessions to split into
     * @param scoreService   used to recompute priority on each session
     * @return list of session tasks (size 1 if no split needed)
     */
    public static List<Task> split(Task task, int numSessions,
                                   PriorityScoreService scoreService) {
        List<Task> sessions = new ArrayList<>();

        // Guard: null or too short to split
        if (task.getEstimatedTime() == null
                || task.getEstimatedTime() < MIN_SPLITTABLE_HOURS
                || numSessions <= 1) {
            sessions.add(task);
            return sessions;
        }

        double sessionLength = task.getEstimatedTime() / numSessions;

        // Guard: each session must be at least 30 minutes
        if (sessionLength < MIN_SESSION_HOURS) {
            sessions.add(task);
            return sessions;
        }

        // Round session length to nearest 30-minute increment
        sessionLength = Math.round(sessionLength * 2.0) / 2.0;

        for (int i = 1; i <= numSessions; i++) {
            String sessionName = task.getName()
                    + " (Part " + i + "/" + numSessions + ")";

            Task session = new Task(
                    sessionName,
                    task.getTaskType(),
                    sessionLength,
                    task.getDueDate(),
                    task.getGradeWeight(),
                    task.getCurrentGrade()
                    // priority left null — recomputed below
            );

            // Recompute priority score for this session
            if (scoreService != null) {
                scoreService.applyPriorityScore(session);
            }

            sessions.add(session);
        }

        return sessions;
    }

    /**
     * Convenience overload — splits into 30-minute sessions automatically.
     * The number of sessions is calculated from the task's estimated time.
     *
     * Example: 2-hour essay → 4 sessions of 30 minutes each.
     *
     * @param task         the task to split
     * @param scoreService used to recompute priority on each session
     * @return list of 30-minute session tasks
     */
    public static List<Task> splitInto30MinSessions(Task task,
                                                     PriorityScoreService scoreService) {
        if (task.getEstimatedTime() == null
                || task.getEstimatedTime() < MIN_SPLITTABLE_HOURS) {
            List<Task> result = new ArrayList<>();
            result.add(task);
            return result;
        }

        int numSessions = (int) Math.ceil(task.getEstimatedTime() / MIN_SESSION_HOURS);
        return split(task, numSessions, scoreService);
    }

    /**
     * Returns true if the task is eligible for splitting.
     *
     * @param task the task to check
     * @return true if estimatedTime >= 30 minutes
     */
    public static boolean canSplit(Task task) {
        return task.getEstimatedTime() != null
                && task.getEstimatedTime() >= MIN_SPLITTABLE_HOURS;
    }
}
