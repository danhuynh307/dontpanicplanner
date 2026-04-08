package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeeklyScheduleService {

    public WeeklyScheduleResponse generateWeeklySchedule(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);

        List<ScheduledTaskBlock> blocks = new ArrayList<>();

        // temp blocks
        // replace with schedule service later
        blocks.add(new ScheduledTaskBlock(1L, "Software Engineering", 2, "10:30", "12:00", "#cf9b7d"));
        blocks.add(new ScheduledTaskBlock(2L, "Computer Systems", 2, "13:30", "15:00", "#89b86d"));

        return new WeeklyScheduleResponse(
                weekStart.toString(),
                weekEnd.toString(),
                blocks
        );
    }
}