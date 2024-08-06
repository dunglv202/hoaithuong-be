package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import dev.dunglv202.hoaithuong.mapper.LectureMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static dev.dunglv202.hoaithuong.constant.Configuration.TEACHER_CODE;

@Entity
@Getter
@Setter
public class Lecture extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TutorClass tutorClass;

    private String topic;

    private String comment;

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

    public Lecture merge(UpdatedLecture updatedLecture) {
        LectureMapper.INSTANCE.mergeLecture(this, updatedLecture);
        return this;
    }
}
