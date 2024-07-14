package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;

@Entity
@Data
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TutorClass tutorClass;

    private Instant startTime;

    private Instant endTime;

    private String topic;

    private String notes;

    private int lectureNo;

    public String getGeneratedCode(ZoneId timeZone) {
        return tutorClass.getCode() + "I26" + DateTimeFmt.MMM.format(startTime.atZone(timeZone)) + lectureNo;
    }
}
