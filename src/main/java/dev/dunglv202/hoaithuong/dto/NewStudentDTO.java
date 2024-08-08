package dev.dunglv202.hoaithuong.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
public class NewStudentDTO {
    @NotBlank(message = "{student.name.not_blank}")
    private String name;

    @Length(max = 256, message = "{student.notes.length}")
    private String notes;
}
