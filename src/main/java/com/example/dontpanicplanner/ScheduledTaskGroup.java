package com.example.dontpanicplanner;

import java.util.List;

public class ScheduledTaskGroup {
    private List<AvailabilityBlock> availabilityBlocks;
    private List<Task> tasks;

    public ScheduledTaskGroup(List<AvailabilityBlock> availabilityBlocks, List<Task> tasks) {
        this.availabilityBlocks = availabilityBlocks;
        this.tasks = tasks;
    }

    public List<AvailabilityBlock> getAvailabilityBlocks() {
        return availabilityBlocks;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}