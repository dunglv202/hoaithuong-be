package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LectureDTO {
    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String classCode;
    private StudentDTO student;
    private String topic;
    private Integer lectureNo;
    private int totalLecture;
    private String comment;
    private String notes;
}
