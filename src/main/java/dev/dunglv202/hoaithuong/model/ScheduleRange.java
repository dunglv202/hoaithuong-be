package dev.dunglv202.hoaithuong.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ScheduleRange implements Range<LocalDate> {
    @NotNull(message = "{schedule.range.invalid}")
    private LocalDate from;

    @NotNull(message = "{schedule.range.invalid}")
    private LocalDate to;
}
