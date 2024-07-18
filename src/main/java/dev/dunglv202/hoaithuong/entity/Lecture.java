package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    private String topic;

    private String notes;

    private Integer lectureNo;

    @OneToOne
    private Schedule schedule;

    public LocalDateTime getStartTime() {
        return schedule.getStartTime();
    }

    public LocalDateTime getEndTime() {
        return schedule.getEndTime();
    }

    public String getGeneratedCode() {
        return tutorClass.getCode() + TEACHER_CODE + DateTimeFmt.MMM.format(getStartTime()).toUpperCase() + lectureNo;
    }
}
