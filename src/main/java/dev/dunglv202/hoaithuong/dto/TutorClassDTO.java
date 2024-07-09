package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.TutorClass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorClassDTO {
    private Long id;
    private String code;
    private StudentDTO student;
    private String level;
    private Integer totalLecture;
    private Integer learned;
    private String notes;

    public TutorClassDTO(TutorClass tutorClass) {
        this.id = tutorClass.getId();
        this.code = tutorClass.getCode();
        this.student = new StudentDTO(tutorClass.getStudent());
        this.level = tutorClass.getLevel().getLabel();
        this.totalLecture = tutorClass.getTotalLecture();
        this.learned = tutorClass.getLearned();
        this.notes = tutorClass.getNotes();
    }
}
