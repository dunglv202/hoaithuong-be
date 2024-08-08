package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TutorClassDTO tutorClass;
    private LectureDTO lecture;
}
