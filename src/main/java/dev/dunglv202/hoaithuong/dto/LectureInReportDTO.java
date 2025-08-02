package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.constant.TutorClassType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LectureInReportDTO {
    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TutorClassType type;
    private String classCode;
    private String student;
    private String topic;
    private String video;
    private Integer lectureNo;
    private int totalLecture;
}
