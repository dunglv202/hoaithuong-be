package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.services.calendar.model.Event;
import dev.dunglv202.hoaithuong.dto.CalendarDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService implements CalendarService {
    private final AuthHelper authHelper;
    private final GoogleHelper googleHelper;

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

    @Override
    public void addEvents(User user, Event... events) {
        // TODO: implement this
    }

    @Override
    public void removeEvent(Event event) {
        // TODO: implement this
    }
}
