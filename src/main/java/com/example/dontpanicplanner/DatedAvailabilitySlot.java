package com.example.dontpanicplanner;

public class DatedAvailabilitySlot {
    private final String date;
    private final int dayOfWeek;
    private final String startTime;
    private final String endTime;

    public DatedAvailabilitySlot(String date, int dayOfWeek, String startTime, String endTime) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDate() {
        return date;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}