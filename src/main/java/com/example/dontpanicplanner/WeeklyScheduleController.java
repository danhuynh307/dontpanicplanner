package com.example.dontpanicplanner;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "http://localhost:3000")
public class WeeklyScheduleController {

    private final WeeklyScheduleService weeklyScheduleService;

    public WeeklyScheduleController(WeeklyScheduleService weeklyScheduleService) {
        this.weeklyScheduleService = weeklyScheduleService;
    }

    @GetMapping("/weekly")
    public WeeklyScheduleResponse getWeeklySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        return weeklyScheduleService.generateWeeklySchedule(weekStart);
    }
}