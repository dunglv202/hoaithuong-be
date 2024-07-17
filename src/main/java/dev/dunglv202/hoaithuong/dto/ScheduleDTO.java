package dev.dunglv202.hoaithuong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.dunglv202.hoaithuong.entity.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
public class ScheduleDTO {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final TutorClassDTO tutorClass;
    @JsonInclude(NON_NULL)
    private final LectureDTO lecture;

    public ScheduleDTO(Schedule schedule) {
        startTime = schedule.getStartTime();
        endTime = schedule.getEndTime();
        tutorClass = new TutorClassDTO(schedule.getTutorClass());
        lecture = schedule.getLecture() != null ? new LectureDTO(schedule.getLecture()) : null;
    }
}
