package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Lecture;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LectureDTO {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String classCode;
    private final StudentDTO student;
    private final String topic;
    private final Integer lectureNo;
    private final int totalLecture;
    private final String comment;
    private final String notes;

    public LectureDTO(Lecture lecture) {
        startTime = lecture.getStartTime();
        endTime = lecture.getEndTime();
        classCode = lecture.getTutorClass().getCode();
        student = new StudentDTO(lecture.getTutorClass().getStudent());
        topic = lecture.getTopic();
        lectureNo = lecture.getLectureNo();
        totalLecture = lecture.getTutorClass().getTotalLecture();
        comment = lecture.getComment();
        notes = lecture.getNotes();
    }
}
