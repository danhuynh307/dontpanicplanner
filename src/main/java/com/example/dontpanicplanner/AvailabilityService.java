package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {
    private List<AvailabilityBlock> availabilityBlocks = createDefaultAvailability();

    public List<AvailabilityBlock> getAvailability() {
        return availabilityBlocks;
    }

    public List<AvailabilityBlock> saveAvailability(List<AvailabilityBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            availabilityBlocks = createDefaultAvailability();
        } else {
            availabilityBlocks = new ArrayList<>(blocks);
        }
        return availabilityBlocks;
    }

    private List<AvailabilityBlock> createDefaultAvailability() {
        List<AvailabilityBlock> defaults = new ArrayList<>();

        defaults.add(new AvailabilityBlock("SUNDAY", "09:00", "17:00"));
        defaults.add(new AvailabilityBlock("MONDAY", "09:00", "17:00"));
        defaults.add(new AvailabilityBlock("TUESDAY", "09:00", "17:00"));
        defaults.add(new AvailabilityBlock("WEDNESDAY", "09:00", "17:00"));
        defaults.add(new AvailabilityBlock("THURSDAY", "09:00", "17:00"));
        defaults.add(new AvailabilityBlock("FRIDAY", "09:00", "17:00"));
        defaults.add(new AvailabilityBlock("SATURDAY", "09:00", "17:00"));

        return defaults;
    }
}