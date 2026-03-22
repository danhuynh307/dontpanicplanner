package com.example.dontpanicplanner;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/availability")
@CrossOrigin(origins = "http://localhost:3000")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<AvailabilityBlock> getAvailability() {
        return availabilityService.getAvailability();
    }

    @PostMapping
    public List<AvailabilityBlock> saveAvailability(@RequestBody List<AvailabilityBlock> blocks) {
        return availabilityService.saveAvailability(blocks);
    }
}