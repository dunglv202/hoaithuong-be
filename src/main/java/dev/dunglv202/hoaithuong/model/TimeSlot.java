package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.constant.Weekday;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
public class TimeSlot implements Comparable<TimeSlot> {
    @NotNull(message = "{timeslot.weekday.required}")
    private Weekday weekday;

    @NotNull(message = "{timeslot.start_time.required}")
    private LocalTime startTime;

    @Null(message = "{timeslot.end_time}")
    private LocalTime endTime;

    public boolean overlaps(@Nonnull TimeSlot another) {
        return this.weekday == another.weekday
            && (startTime.isBefore(another.endTime) || endTime.isAfter(another.startTime));
    }

    @Override
    public int compareTo(@Nonnull TimeSlot another) {
        return 0;
    }
}
