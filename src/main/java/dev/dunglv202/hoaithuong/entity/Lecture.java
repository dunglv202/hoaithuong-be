package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.constant.LectureStatus;
import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;

import static dev.dunglv202.hoaithuong.constant.Configuration.TEACHER_CODE;

@Entity
@Getter
@Setter
public class Lecture extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TutorClass tutorClass;

    private Instant startTime;

    private String topic;

    private String notes;

    private Integer lectureNo;

    @Enumerated
    private LectureStatus status;

    public Instant getEndTime() {
        return startTime.plus(tutorClass.getDuration());
    }

    public String getGeneratedCode(ZoneId timeZone) {
        return tutorClass.getCode() + TEACHER_CODE + DateTimeFmt.MMM.format(startTime.atZone(timeZone)).toUpperCase() + lectureNo;
    }
}
