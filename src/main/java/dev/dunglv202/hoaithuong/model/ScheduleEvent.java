package dev.dunglv202.hoaithuong.model;

import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.calendar.model.Event;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.helper.CalendarHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ScheduleEvent {
    private final Schedule schedule;
    private final Event event;

    public ScheduleEvent(Schedule schedule) {
        this.schedule = schedule;
        var calendarHelper = new CalendarHelper();
        if (schedule.getGoogleEventId() != null) {
            this.event = new Event().setId(schedule.getGoogleEventId());
        } else {
            var title = String.format(
                "%s",
                schedule.getTutorClass().getName()
            );
            this.event = new Event()
                .setSummary(title)
                .setStart(calendarHelper.toEventDateTime(schedule.getStartTime()))
                .setEnd(calendarHelper.toEventDateTime(schedule.getEndTime()));
        }
    }

    public JsonBatchCallback<Event> getCallback() {
        return new JsonBatchCallback<>() {
            @Override
            public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
                log.error("Could not add event {}: {}", event, googleJsonError);
                throw new RuntimeException("Could not add event");
            }

            @Override
            public void onSuccess(Event event, HttpHeaders httpHeaders) {
                schedule.setGoogleEventId(event.getId());
            }
        };
    }
}
