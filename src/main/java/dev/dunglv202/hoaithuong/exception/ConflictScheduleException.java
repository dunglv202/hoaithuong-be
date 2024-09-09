package dev.dunglv202.hoaithuong.exception;

import dev.dunglv202.hoaithuong.entity.Schedule;
import org.springframework.http.HttpStatus;

import static dev.dunglv202.hoaithuong.helper.DateTimeFmt.FULL;

public class ConflictScheduleException extends ClientVisibleException {
    public ConflictScheduleException(Schedule scheduled) {
        super(HttpStatus.CONFLICT, null, "{tutor_class.timeslots.invalid}: " + scheduled.getStartTime().format(FULL));
    }
}
