package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.TutorClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class NewTutorClassDTO {
    @NotBlank(message = "{tutor_class.code.not_blank}")
    private String code;

    @NotNull(message = "{tutor_class.student.required}")
    private Long studentId;

    @NotNull(message = "{tutor_class.level.required}")
    private String level;

    @Positive(message = "{tutor_class.total_lecture.positive}")
    private int totalLecture;

    @Positive(message = "{tutor_class.learned.positive}")
    private Integer learned;

    @Length(max = 256, message = "{tutor_class.notes.length}")
    private String notes;

    public TutorClass toEntity() {
        TutorClass tutorClass = new TutorClass();
        tutorClass.setCode(code);
        tutorClass.setNotes(notes);
        tutorClass.setTotalLecture(totalLecture);
        tutorClass.setLevel(level);
        tutorClass.setLearned(learned == null ? 0 : learned);
        return tutorClass;
    }
}
