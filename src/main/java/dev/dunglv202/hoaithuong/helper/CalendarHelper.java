package dev.dunglv202.hoaithuong.helper;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Helper for Google Calendar
 */
public class CalendarHelper {
    public EventDateTime toEventDateTime(LocalDateTime localDateTime) {
        var dateTime = new DateTime(localDateTime.atZone(ZoneOffset.ofHours(7)).toInstant().toEpochMilli());
        return new EventDateTime().setDateTime(dateTime);
    }
}
