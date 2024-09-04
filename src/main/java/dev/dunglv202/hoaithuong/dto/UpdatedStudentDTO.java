package dev.dunglv202.hoaithuong.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UpdatedStudentDTO {
    private Long id;

    @NotBlank(message = "{student.name.not_blank}")
    private String name;

    @Length(max = 256, message = "{student.notes.length}")
    private String notes;

    @NotNull(message = "{student.report_to.required}")
    @Valid
    private PersonDTO reportTo;
}
