package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.CalendarDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.ScheduleEvent;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * Service for interacting with Google Calendar APIs
 */
public interface CalendarService {
    List<CalendarDTO> getAllCalendars();

    void addEvents(User user, List<ScheduleEvent> schedules);

    @Async
    void addEventsAsync(User user, List<ScheduleEvent> schedules);

    void removeEvents(User user, List<ScheduleEvent> schedules);

    @Async
    void removeEventsAsync(User user, List<ScheduleEvent> schedules);

    boolean isValidCalendar(User user, String calendarId);
}
