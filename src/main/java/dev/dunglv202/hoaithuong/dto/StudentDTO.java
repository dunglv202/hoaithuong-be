package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Student;
import lombok.Getter;

@Getter
public class StudentDTO {
    private final Long id;
    private final String name;
    private final String notes;

    public StudentDTO(Student student) {
        this.id = student.getId();
        this.name = student.getName();
        this.notes = student.getNotes();
    }
}
