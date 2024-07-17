package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.ScheduleDTO;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.ScheduleRange;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ScheduleDTO> getSchedule(@Valid ScheduleRange range) {
        return scheduleService.getSchedule(range);
    }
}
