package com.example.dontpanicplanner;

import java.util.List;

public class WeeklyScheduleResponse {
    private String weekStart;
    private String weekEnd;
    private List<ScheduledTaskBlock> blocks;

    public WeeklyScheduleResponse() {}

    public WeeklyScheduleResponse(String weekStart, String weekEnd, List<ScheduledTaskBlock> blocks) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.blocks = blocks;
    }

    public String getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    public String getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(String weekEnd) {
        this.weekEnd = weekEnd;
    }

    public List<ScheduledTaskBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<ScheduledTaskBlock> blocks) {
        this.blocks = blocks;
    }
}