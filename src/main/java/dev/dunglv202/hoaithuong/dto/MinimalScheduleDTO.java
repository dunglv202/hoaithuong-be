package dev.dunglv202.hoaithuong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MinimalScheduleDTO {
    private long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private ScheduleClassDTO tutorClass;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ScheduleLectureDTO lecture;
}
