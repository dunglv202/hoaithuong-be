package dev.dunglv202.hoaithuong.rest;

import dev.dunglv202.hoaithuong.dto.MinimalScheduleDTO;
import dev.dunglv202.hoaithuong.dto.NewScheduleDTO;
import dev.dunglv202.hoaithuong.model.ScheduleRange;
import dev.dunglv202.hoaithuong.model.SimpleRange;
import dev.dunglv202.hoaithuong.service.impl.ScheduleServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleServiceImpl scheduleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MinimalScheduleDTO> getSchedule(@Valid ScheduleRange range) {
        return scheduleService.getSchedules(range);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void addSchedule(@Valid @RequestBody NewScheduleDTO newSchedule) {
        scheduleService.addNewSchedule(newSchedule);
    }

    @PostMapping("/sync_calendar")
    @PreAuthorize("isAuthenticated()")
    public void syncToGoogleCalendar(@RequestBody SimpleRange<LocalDate> range) {
        scheduleService.syncToCalendar(range);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteSchedule(@PathVariable long id) {
        scheduleService.deleteSchedule(id);
    }
}
