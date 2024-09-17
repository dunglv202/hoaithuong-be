package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import dev.dunglv202.hoaithuong.dto.CalendarDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.model.ScheduleEvent;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.service.CalendarService;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final ScheduleRepository scheduleRepository;

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
            scheduleRepository.saveAll(schedules.stream().map(ScheduleEvent::getSchedule).toList());
        } catch (IOException e) {
            log.info("Could not these events to calendar", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async
    public void addEventsAsync(User user, List<ScheduleEvent> schedules) {
        addEvents(user, schedules);
    }

    @Override
    public void removeEvents(User user, List<ScheduleEvent> schedules) {
        if (schedules.isEmpty()) return;

        Configuration configs = configService.getConfigsByUser(user);
        var calendarService = googleHelper.getCalendarService(user);
        var eventService = calendarService.events();

        // add to batch
        var batch = calendarService.batch();
        var callback = new JsonBatchCallback<Void>() {
            @Override
            public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) throws IOException {
                log.error("Could not delete event {}: ", googleJsonError);
                throw new RuntimeException("Could not delete event");
            }

            @Override
            public void onSuccess(Void unused, HttpHeaders httpHeaders) {
            }
        };
        schedules.forEach(schedule -> {
            try {
                eventService.delete(configs.getCalendarId(), schedule.getEvent().getId())
                    .queue(batch, callback);
            } catch (IOException e) {
                log.error("Could not delete event", e);
                throw new RuntimeException(e);
            }
        });

        // execute to delete &
        try {
            batch.execute();
        } catch (IOException e) {
            log.info("Could not remove these events from calendar", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async
    public void removeEventsAsync(User user, List<ScheduleEvent> schedules) {
        removeEvents(user, schedules);
    }
}
