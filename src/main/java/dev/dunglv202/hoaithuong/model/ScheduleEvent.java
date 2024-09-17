package dev.dunglv202.hoaithuong.model;

import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.calendar.model.Event;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.helper.CalendarHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Getter
@Slf4j
public class ScheduleEvent {
    private final Schedule schedule;
    private final Event event;

    public ScheduleEvent(Schedule schedule) {
        this.schedule = schedule;
        var calendarHelper = new CalendarHelper();
        var title = String.format(
            "%s",
            schedule.getTutorClass().getName()
        );
        this.event = new Event()
            .setSummary(title)
            .setStart(calendarHelper.toEventDateTime(schedule.getStartTime()))
            .setEnd(calendarHelper.toEventDateTime(schedule.getEndTime()));
    }

    public JsonBatchCallback<Event> getCallback() {
        return new JsonBatchCallback<>() {
            @Override
            public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) throws IOException {
                log.error("Could not add event {}: {}", event, googleJsonError);
            }

            @Override
            public void onSuccess(Event event, HttpHeaders httpHeaders) throws IOException {
                schedule.setGoogleEventId(event.getId());
            }
        };
    }
}
