package dev.dunglv202.hoaithuong.service;

import com.google.api.services.calendar.model.Event;
import dev.dunglv202.hoaithuong.dto.CalendarDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.ScheduleEvent;

import java.util.List;

/**
 * Service for interacting with Google Calendar APIs
 */
public interface CalendarService {
    List<CalendarDTO> getAllCalendars();

    void addEvents(User user, List<ScheduleEvent> events);

    void removeEvent(Event event);
}
