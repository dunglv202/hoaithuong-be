package dev.dunglv202.hoaithuong.helper;

import java.time.format.DateTimeFormatter;

public class DateTimeFmt {
    public static final DateTimeFormatter D_M_YYYY = DateTimeFormatter.ofPattern("d/M/yyyy");
    public static final DateTimeFormatter H_M = DateTimeFormatter.ofPattern("H'h'm");
    public static final DateTimeFormatter MMM = DateTimeFormatter.ofPattern("MMM");
}
