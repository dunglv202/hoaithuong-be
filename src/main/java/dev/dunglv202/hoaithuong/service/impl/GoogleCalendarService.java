package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.calendar.Calendar;
import dev.dunglv202.hoaithuong.constant.ApiErrorCode;
import dev.dunglv202.hoaithuong.dto.CalendarDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.Notification;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.AuthenticationException;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.model.ScheduleEvent;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.service.CalendarService;
import dev.dunglv202.hoaithuong.service.ConfigService;
import dev.dunglv202.hoaithuong.service.NotificationService;
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
    private final NotificationService notificationService;

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

        var calendarService = googleHelper.getCalendarService(user);
        var eventService = calendarService.events();
        Configuration configs = configService.getConfigsByUser(user);
        if (!isValidCalendar(calendarService, configs.getCalendarId())) {
            throw new ClientVisibleException("{google.calendar.invalid}");
        }

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
        try {
            addEvents(user, schedules);
        } catch (Exception e) {
            log.error("Could not add events for", e);
            var notification = Notification.forUser(user)
                .content("Failed to sync your calendar! You might want to do that manually");
            notificationService.addNotification(notification);
        }
    }

    @Override
    public void removeEvents(User user, List<ScheduleEvent> schedules) {
        if (schedules.isEmpty()) return;

        Configuration configs = configService.getConfigsByUser(user);
        var calendarService = googleHelper.getCalendarService(user);
        var eventService = calendarService.events();
        if (!isValidCalendar(calendarService, configs.getCalendarId())) {
            throw new ClientVisibleException("{google.calendar.invalid}");
        }

        // add to batch
        var batch = calendarService.batch();
        var callback = new JsonBatchCallback<Void>() {
            @Override
            public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
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
        try {
            removeEvents(user, schedules);
        } catch (Exception e) {
            log.error("Could not remove events", e);
            var notification = Notification.forUser(user)
                .content("Failed to sync your calendar! You might want to do that manually");
            notificationService.addNotification(notification);
        }
    }

    private boolean isValidCalendar(Calendar calendarService, String calendarId) {
        try {
            if (calendarId == null) return false;
            calendarService.calendarList().get(calendarId).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) return false;
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
                throw new AuthenticationException(ApiErrorCode.REQUIRE_GOOGLE_AUTH);
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
