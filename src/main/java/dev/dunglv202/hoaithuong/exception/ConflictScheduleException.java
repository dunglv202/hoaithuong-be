package dev.dunglv202.hoaithuong.exception;

public class ConflictScheduleException extends ClientVisibleException {
    public ConflictScheduleException() {
        super("{tutor_class.timeslots.invalid}");
    }
}
