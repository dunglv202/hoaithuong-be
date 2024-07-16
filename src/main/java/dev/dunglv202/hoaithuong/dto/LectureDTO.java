package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.constant.LectureStatus;
import dev.dunglv202.hoaithuong.entity.Lecture;
import lombok.Getter;

import java.time.Instant;

@Getter
public class LectureDTO {
    private final Instant startTime;
    private final Instant endTime;
    private final String classCode;
    private final StudentDTO student;
    private final String topic;
    private final Integer lectureNo;
    private final int totalLecture;
    private final LectureStatus status;

    public LectureDTO(Lecture lecture) {
        startTime = lecture.getStartTime();
        endTime = lecture.getEndTime();
        classCode = lecture.getTutorClass().getCode();
        student = new StudentDTO(lecture.getTutorClass().getStudent());
        topic = lecture.getTopic();
        lectureNo = lecture.getLectureNo();
        totalLecture = lecture.getTutorClass().getTotalLecture();
        status = lecture.getStatus();
    }
}
