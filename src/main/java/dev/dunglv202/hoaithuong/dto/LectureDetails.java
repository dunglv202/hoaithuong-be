package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LectureDetails {
    private long id;
    private TutorClassDTO tutorClass;
    private LocalDateTime startTime;
    private String topic;
    private String video;
    private boolean hasPreview;
    private String comment;
    private String notes;
    private int lectureNo;
}
