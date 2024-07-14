package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;

import static dev.dunglv202.hoaithuong.constant.Configuration.TEACHER_CODE;

@Entity
@Data
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TutorClass tutorClass;

    private Instant startTime;

    private String topic;

    private String notes;

    private int lectureNo;

    public Instant getEndTime() {
        return startTime.plus(tutorClass.getDuration());
    }

    public String getGeneratedCode(ZoneId timeZone) {
        return tutorClass.getCode() + TEACHER_CODE + DateTimeFmt.MMM.format(startTime.atZone(timeZone)).toUpperCase() + lectureNo;
    }
}
