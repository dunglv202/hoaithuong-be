package dev.dunglv202.hoaithuong.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LectureInReportDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String classCode;
    private StudentInReportDTO student;
    private String topic;
    private Integer lectureNo;
    private int totalLecture;
}
