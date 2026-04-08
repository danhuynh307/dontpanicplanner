package com.example.dontpanicplanner;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "tasks")
@Access(AccessType.FIELD)   // JPA reads fields directly — no getters/setters required
public class Task {

    // ── CSV header matches field order in toCSV() / fromCSV() ──
    static final String CSV_HEADER =
            "name,taskType,estimatedTime,dueDate,gradeWeight,currentGrade,priorityScore";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    // ── Fields ────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String   name;
    String   taskType;
    Double   estimatedTime;
    LocalDate dueDate;
    Integer  gradeWeight;
    Double   currentGrade;
    Integer  priorityScore;   // null until the priority algorithm runs


    // Required by JPA
    public Task() {}

    // Full — used when every field is known (e.g. importing from CSV)
    public Task(String name, String taskType, Double estimatedTime,
                LocalDate dueDate, Integer gradeWeight,
                Double currentGrade, Integer priorityScore) {
        this.name          = name;
        this.taskType      = taskType;
        this.estimatedTime = estimatedTime;
        this.dueDate       = dueDate;
        this.gradeWeight   = gradeWeight;
        this.currentGrade  = currentGrade;
        this.priorityScore = priorityScore;
    }

    // Without priority — priority score is not calculated at creation time
    public Task(String name, String taskType, Double estimatedTime,
                LocalDate dueDate, Integer gradeWeight, Double currentGrade) {
        this(name, taskType, estimatedTime, dueDate, gradeWeight, currentGrade, null);
    }

    // Minimal — only the essentials, everything else left null
    public Task(String name, LocalDate dueDate) {
        this.name    = name;
        this.dueDate = dueDate;
    }


    // converts task into a CSV row
    public String toCSV() {
        return String.join(",",
                csvEscape(name),
                csvEscape(taskType),
                estimatedTime  != null ? String.valueOf(estimatedTime)  : "",
                dueDate        != null ? dueDate.format(DATE_FMT)       : "",
                gradeWeight    != null ? String.valueOf(gradeWeight)     : "",
                currentGrade   != null ? String.valueOf(currentGrade)    : "",
                priorityScore  != null ? String.valueOf(priorityScore)   : ""
        );
    }


    // creates a Task from a CSV row
    public static Task fromCSV(String csvLine) {
        String[] p = splitCSVLine(csvLine);

        if (p.length < 7) {
            throw new IllegalArgumentException(
                    "CSV row needs 7 columns, got " + p.length + ": " + csvLine);
        }

        Task t = new Task();
        t.name          = p[0];
        t.taskType      = p[1];
        t.estimatedTime = blank(p[2])  ? null : Double.parseDouble(p[2]);
        t.dueDate       = blank(p[3])  ? null : LocalDate.parse(p[3], DATE_FMT);
        t.gradeWeight   = blank(p[4])  ? null : Integer.parseInt(p[4]);
        t.currentGrade  = blank(p[5])  ? null : Double.parseDouble(p[5]);
        t.priorityScore = blank(p[6])  ? null : Integer.parseInt(p[6]);
        return t;
    }


    // Wraps value in double-quotes if it contains a comma or quote
    private static String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Splits a CSV line, respecting double-quoted fields
    private static String[] splitCSVLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // Escaped quote inside a quoted field ("" → ")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());   // last field
        return fields.toArray(new String[0]);
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    // Getters

    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getTaskType()      { return taskType; }
    public Double getEstimatedTime() { return estimatedTime; }
    public LocalDate getDueDate()    { return dueDate; }
    public Integer getGradeWeight()  { return gradeWeight; }
    public Double getCurrentGrade()  { return currentGrade; }
    public Integer getPriorityScore(){ return priorityScore; }

    // Setters

    public void setId(Long id)                      { this.id = id; }
    public void setName(String name)                { this.name = name; }
    public void setTaskType(String taskType)        { this.taskType = taskType; }
    public void setEstimatedTime(Double t)          { this.estimatedTime = t; }
    public void setDueDate(LocalDate dueDate)       { this.dueDate = dueDate; }
    public void setGradeWeight(Integer gradeWeight) { this.gradeWeight = gradeWeight; }
    public void setCurrentGrade(Double currentGrade){ this.currentGrade = currentGrade; }
    public void setPriorityScore(Integer score)     { this.priorityScore = score; }

    @Override
    public String toString() {
        return "Task{id=" + id +
                ", name='"         + name          + '\'' +
                ", taskType='"     + taskType       + '\'' +
                ", estimatedTime=" + estimatedTime  +
                ", dueDate="       + dueDate        +
                ", gradeWeight="   + gradeWeight    +
                ", currentGrade="  + currentGrade   +
                ", priorityScore=" + priorityScore  +
                '}';
    }
}
