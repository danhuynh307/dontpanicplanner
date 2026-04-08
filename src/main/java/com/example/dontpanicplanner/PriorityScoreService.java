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

    // calculates a priority score from 0–100 based on task properties
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

    // calculates the score and stores it back into the task
    public Task applyPriorityScore(Task task) {
        if (task == null) {
            return null;
        }

        task.setPriorityScore(calculatePriorityScore(task));
        return task;
    }

    // shorter tasks get higher priority, longer tasks get lower
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

    // tasks due sooner get higher priority, far deadlines get lower
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

    // higher grade weight means higher priority
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

    // lower current grade = higher priority, high grade = lower priority
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
