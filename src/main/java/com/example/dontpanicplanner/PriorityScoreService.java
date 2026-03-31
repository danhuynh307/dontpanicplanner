package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PriorityScoreService {

    // Weights from your team draft
    private static final double ESTIMATED_TIME_WEIGHT = 0.15;
    private static final double DUE_DATE_WEIGHT = 0.40;
    private static final double GRADE_WEIGHT_WEIGHT = 0.20;
    private static final double CURRENT_GRADE_WEIGHT = 0.25;

    /**
     * Calculates and returns an integer priority score from 0 to 100.
     * Also leaves scheduling/splitting/ranking concerns alone since those belong
     * to the other team tasks.
     */
    public int calculatePriorityScore(Task task) {
        if (task == null) {
            return 0;
        }

        double estimatedTimeScore = calculateEstimatedTimeScore(task.getEstimatedTime());
        double dueDateScore = calculateDueDateScore(task.getDueDate());
        double gradeWeightScore = calculateGradeWeightScore(task.getGradeWeight());
        double currentGradeScore = calculateCurrentGradeScore(task.getCurrentGrade());

        double weightedScore =
                (estimatedTimeScore * ESTIMATED_TIME_WEIGHT) +
                        (dueDateScore * DUE_DATE_WEIGHT) +
                        (gradeWeightScore * GRADE_WEIGHT_WEIGHT) +
                        (currentGradeScore * CURRENT_GRADE_WEIGHT);

        return (int) Math.round(weightedScore * 100.0);
    }

    /**
     * Recomputes the priority score and stores it directly on the task.
     */
    public Task applyPriorityScore(Task task) {
        if (task == null) {
            return null;
        }

        task.setPriorityScore(calculatePriorityScore(task));
        return task;
    }

    /**
     * Estimated Time formula draft:
     * (5 - t) / 4.5
     *
     * Intended behavior:
     * - 0.5 hours or less => max priority contribution
     * - 5 hours or more => min priority contribution
     * - shorter tasks score higher
     */
    private double calculateEstimatedTimeScore(Double estimatedTimeHours) {
        if (estimatedTimeHours == null) {
            // Neutral-to-high default if missing
            return 1.0;
        }

        double t = estimatedTimeHours;

        if (t <= 0.5) {
            return 1.0;
        }
        if (t >= 5.0) {
            return 0.0;
        }

        return clamp((5.0 - t) / 4.5);
    }

    /**
     * Due Date formula draft:
     * (10 - d) / 9
     *
     * Intended behavior:
     * - due in 1 day or less => max priority contribution
     * - due in 10 days or more => min priority contribution
     * - overdue tasks are treated as max urgency
     */
    private double calculateDueDateScore(LocalDate dueDate) {
        if (dueDate == null) {
            return 0.0;
        }

        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

        if (daysUntilDue <= 1) {
            return 1.0;
        }
        if (daysUntilDue >= 10) {
            return 0.0;
        }

        return clamp((10.0 - daysUntilDue) / 9.0);
    }

    /**
     * Grade Weight formula draft:
     * (w - 1) / 19
     *
     * Intended behavior:
     * - 1% or less => minimum contribution
     * - 20% or more => maximum contribution
     */
    private double calculateGradeWeightScore(Integer gradeWeightPercent) {
        if (gradeWeightPercent == null) {
            return 0.0;
        }

        double w = gradeWeightPercent;

        if (w <= 1.0) {
            return 0.0;
        }
        if (w >= 20.0) {
            return 1.0;
        }

        return clamp((w - 1.0) / 19.0);
    }

    /**
     * Current Grade formula draft:
     * (90 - g) / 20
     *
     * Intended behavior:
     * - below 70 => maximum contribution
     * - 90 or above => minimum contribution
     *
     * Team note says this may not be implemented in the MVP and should default to max.
     * So if currentGrade is null, we use 1.0.
     */
    private double calculateCurrentGradeScore(Double currentGradePercent) {
        if (currentGradePercent == null) {
            return 1.0;
        }

        double g = currentGradePercent;

        if (g <= 70.0) {
            return 1.0;
        }
        if (g >= 90.0) {
            return 0.0;
        }

        return clamp((90.0 - g) / 20.0);
    }

    private double clamp(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
