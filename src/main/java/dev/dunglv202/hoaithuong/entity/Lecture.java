package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import dev.dunglv202.hoaithuong.mapper.LectureMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    private String video;

    private String comment;

    private String notes;

    private Integer lectureNo;

    @OneToOne
    private Schedule schedule;

    /* Teacher can have different code for each lecture ??? :D */
    private String teacherCode;

    @Setter(AccessLevel.PRIVATE)
    @ManyToOne(fetch = FetchType.LAZY)
    private User teacher;

    @PrePersist
    public void prePersist() {
        this.teacher = this.tutorClass.getTeacher();
    }

    public LocalDateTime getStartTime() {
        return schedule.getStartTime();
    }

    public LocalDateTime getEndTime() {
        return schedule.getEndTime();
    }

    public String getGeneratedCode() {
        return tutorClass.getCode() + teacherCode + DateTimeFmt.MMM.format(getStartTime()).toUpperCase() + lectureNo;
    }

    public Lecture merge(UpdatedLecture updatedLecture) {
        LectureMapper.INSTANCE.mergeLecture(this, updatedLecture);
        return this;
    }
}
