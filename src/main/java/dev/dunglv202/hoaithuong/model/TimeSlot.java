package dev.dunglv202.hoaithuong.model;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Setter
@Getter
public class TimeSlot implements Comparable<TimeSlot> {
    @NotNull(message = "{timeslot.weekday.required}")
    private DayOfWeek weekday;

    @NotNull(message = "{timeslot.start_time.required}")
    private LocalTime startTime;

    /**
     * This is set by tutor class to which this time slot belong
     */
    @Null(message = "{timeslot.end_time}")
    private LocalTime endTime;

    public boolean overlaps(@Nonnull TimeSlot another) {
        if (this.weekday != another.weekday) return false;
        return this.startTime.isBefore(another.startTime)
            ? this.endTime.isAfter(another.startTime)
            : this.startTime.isBefore(another.endTime);
    }

    @Override
    public int compareTo(@Nonnull TimeSlot another) {
        if (this.weekday != another.weekday) {
            return this.weekday.compareTo(another.weekday);
        }
        return this.startTime.compareTo(another.startTime);
    }
}
