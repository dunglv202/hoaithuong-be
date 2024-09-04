package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.MinimalScheduleDTO;
import dev.dunglv202.hoaithuong.dto.NewScheduleDTO;
import dev.dunglv202.hoaithuong.model.ScheduleRange;
import dev.dunglv202.hoaithuong.service.impl.ScheduleServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleServiceImpl scheduleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MinimalScheduleDTO> getSchedule(@Valid ScheduleRange range) {
        return scheduleService.getSchedule(range);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void addSchedule(@Valid @RequestBody NewScheduleDTO newSchedule) {
        scheduleService.addNewSchedule(newSchedule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteSchedule(@PathVariable long id) {
        scheduleService.deleteSchedule(id);
    }
}
