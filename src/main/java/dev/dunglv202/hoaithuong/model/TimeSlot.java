package dev.dunglv202.hoaithuong.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot implements Comparable<TimeSlot> {
    @NotNull(message = "{timeslot.weekday.required}")
    private DayOfWeek weekday;

    @NotNull(message = "{timeslot.start_time.required}")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Override
    public int compareTo(@Nonnull TimeSlot another) {
        if (this.weekday != another.weekday) {
            return this.weekday.compareTo(another.weekday);
        }
        return this.startTime.compareTo(another.startTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        return obj instanceof TimeSlot
            && this.weekday == ((TimeSlot) obj).weekday
            && this.startTime.equals(((TimeSlot) obj).startTime);
    }

    @Override
    public int hashCode() {
        return weekday.hashCode();
    }
}
