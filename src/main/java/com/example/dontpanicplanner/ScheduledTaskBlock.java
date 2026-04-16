package com.example.dontpanicplanner;

public class ScheduledTaskBlock {
    private Long taskId;
    private String taskTitle;
    private String date;      // YYYY-MM-DD
    private int dayOfWeek;    // 0 = Sunday, 6 = Saturday
    private String startTime; // hh:mm
    private String endTime;   // hh:mm
    private String color;

    public ScheduledTaskBlock() {}

    public ScheduledTaskBlock(
            Long taskId,
            String taskTitle,
            String date,
            int dayOfWeek,
            String startTime,
            String endTime,
            String color
    ) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}