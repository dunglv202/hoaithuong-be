package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.services.calendar.model.Event;
import dev.dunglv202.hoaithuong.dto.CalendarDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.model.ScheduleEvent;
import dev.dunglv202.hoaithuong.service.CalendarService;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService implements CalendarService {
    private final AuthHelper authHelper;
    private final GoogleHelper googleHelper;
    private final ConfigService configService;

    @Override
    public List<CalendarDTO> getAllCalendars() {
        try {
            User user = authHelper.getSignedUser();
            return googleHelper.getCalendarService(user).calendarList()
                .list().execute().getItems().stream()
                .map(CalendarDTO::new)
                .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add schedule to Google Calendar then update schedule's event id. See {@link Schedule#setGoogleEventId(String)}
     */
    @Override
    public void addEvents(User user, List<ScheduleEvent> schedules) {
        if (schedules.isEmpty()) return;

        Configuration configs = configService.getConfigsByUser(user);
        var calendarService = googleHelper.getCalendarService(user);
        var eventService = calendarService.events();

        // add to batch
        var batch = calendarService.batch();
        schedules.forEach(schedule -> {
            try {
                eventService.insert(configs.getCalendarId(), schedule.getEvent()).queue(batch, schedule.getCallback());
            } catch (IOException e) {
                log.error("Could not add event", e);
                throw new RuntimeException(e);
            }
        });

        // execute to add
        try {
            batch.execute();
        } catch (IOException e) {
            log.info("Could not these events to calendar", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeEvent(Event event) {
        // TODO: implement this
    }
}
