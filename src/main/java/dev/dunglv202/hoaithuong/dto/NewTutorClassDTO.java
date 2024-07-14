package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.TutorClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import static dev.dunglv202.hoaithuong.constant.Configuration.DEFAULT_LECTURE_DURATION_IN_MINUTE;
import static dev.dunglv202.hoaithuong.constant.Configuration.DEFAULT_PAY_FOR_LECTURE;

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

    @PositiveOrZero(message = "{tutor_class.learned.positive}")
    private Integer learned = 0;

    @Length(max = 256, message = "{tutor_class.notes.length}")
    private String notes;

    @Positive(message = "{tutor_class.duration.invalid}")
    private Integer durationInMinute = DEFAULT_LECTURE_DURATION_IN_MINUTE;

    @PositiveOrZero(message = "{tutor_class.pay.positive}")
    private Integer payForLecture = DEFAULT_PAY_FOR_LECTURE;

    public TutorClass toEntity() {
        TutorClass tutorClass = new TutorClass();
        tutorClass.setCode(code);
        tutorClass.setNotes(notes);
        tutorClass.setTotalLecture(totalLecture);
        tutorClass.setLevel(level);
        tutorClass.setLearned(learned);
        tutorClass.setDurationInMinute(durationInMinute);
        tutorClass.setPayForLecture(payForLecture);
        return tutorClass;
    }
}
