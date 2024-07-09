package dev.dunglv202.hoaithuong.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.YearMonth;

@Setter
@Getter
public class ReportRange {
    private int month;
    private int year;

    public LocalDate getStartDate() {
        return LocalDate.of(year, month, 1);
    }

    public LocalDate getEndDate() {
        return YearMonth.of(year, month).atEndOfMonth();
    }
}
