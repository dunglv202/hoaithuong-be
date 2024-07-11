package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.TutorClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewTutorClassDTO {
    @NotBlank(message = "{tutor_class.code.not_blank}")
    private String code;

    @NotNull(message = "{tutor_class.student.required}")
    private Long studentId;

    @NotNull(message = "{tutor_class.level.required}")
    private String level;

    @NotNull(message = "{tutor_class.total_lecture.required}")
    private Integer totalLecture;

    private String notes;

    public TutorClass toEntity() {
        TutorClass tutorClass = new TutorClass();
        tutorClass.setCode(code);
        tutorClass.setNotes(notes);
        tutorClass.setTotalLecture(totalLecture);
        tutorClass.setLevel(level);
        return tutorClass;
    }
}
