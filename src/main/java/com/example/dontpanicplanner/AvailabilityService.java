package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {
    private List<AvailabilityBlock> availabilityBlocks = new ArrayList<>();

    public List<AvailabilityBlock> getAvailability() {
        return availabilityBlocks;
    }

    public List<AvailabilityBlock> saveAvailability(List<AvailabilityBlock> blocks) {
        availabilityBlocks = new ArrayList<>(blocks);
        return availabilityBlocks;
    }
}