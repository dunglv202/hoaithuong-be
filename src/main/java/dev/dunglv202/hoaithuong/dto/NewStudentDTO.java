package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Student;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
public class NewStudentDTO {
    @NotBlank(message = "{student.name.not_blank}")
    private String name;

    @Length(max = 256, message = "{student.notes.max_length}")
    private String notes;

    public Student toEntity() {
        Student student = new Student();
        student.setName(this.name);
        student.setNotes(this.notes);
        return student;
    }
}
