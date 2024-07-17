package dev.dunglv202.hoaithuong.model;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.YearMonth;

@Setter
@Getter
public class ReportRange implements Range<LocalDate> {
    @Positive(message = "{range.invalid}")
    private int month;

    @Positive(message = "{range.invalid}")
    private int year;

    @Override
    public LocalDate getFrom() {
        return LocalDate.of(year, month, 1);
    }

    @Override
    public LocalDate getTo() {
        return YearMonth.of(year, month).atEndOfMonth();
    }
}
