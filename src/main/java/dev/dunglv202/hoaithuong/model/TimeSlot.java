package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.constant.Weekday;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
public class TimeSlot {
    @NotNull(message = "{timeslot.weekday.required}")
    private Weekday weekday;

    @NotNull(message = "{timeslot.start_time.required}")
    private LocalTime startTime;
}
