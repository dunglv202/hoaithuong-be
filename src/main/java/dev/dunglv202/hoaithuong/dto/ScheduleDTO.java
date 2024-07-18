package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleDTO {
    private final Long id;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final TutorClassDTO tutorClass;
    private final LectureDTO lecture;

    public ScheduleDTO(Schedule schedule) {
        id = schedule.getId();
        startTime = schedule.getStartTime();
        endTime = schedule.getEndTime();
        tutorClass = new TutorClassDTO(schedule.getTutorClass());
        lecture = schedule.getLecture() != null ? new LectureDTO(schedule.getLecture()) : null;
    }
}
