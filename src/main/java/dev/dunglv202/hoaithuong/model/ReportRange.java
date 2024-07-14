package dev.dunglv202.hoaithuong.model;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

@Setter
@Getter
public class ReportRange {
    private ZoneId timeZone = LocaleContextHolder.getTimeZone().toZoneId();

    @Positive(message = "{range.invalid}")
    private int month;

    @Positive(message = "{range.invalid}")
    private int year;

    public Instant getStartTime() {
        return LocalDate.of(year, month, 1)
            .atStartOfDay(timeZone)
            .toInstant();
    }

    public Instant getEndTime() {
        return YearMonth.of(year, month).atEndOfMonth()
            .atStartOfDay(timeZone)
            .toInstant();
    }
}
