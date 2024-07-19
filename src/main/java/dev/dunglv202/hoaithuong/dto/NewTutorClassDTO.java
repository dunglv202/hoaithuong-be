package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

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

    @NotNull(message = "{tutor_class.timeslots.required}")
    @Size(min = 1, message = "{tutor_class.timeslots.required}")
    private Set<@Valid TimeSlot> timeSlots;

    @NotNull(message = "{tutor_class.start_date.required}")
    @FutureOrPresent(message = "{tutor_class.start_data.today_or_future}")
    private LocalDate startDate;

    public TutorClass toEntity() {
        TutorClass tutorClass = new TutorClass();
        tutorClass.setCode(code);
        tutorClass.setNotes(notes);
        tutorClass.setTotalLecture(totalLecture);
        tutorClass.setLevel(level);
        tutorClass.setLearned(learned);
        tutorClass.setInitialLearned(learned);
        tutorClass.setDurationInMinute(durationInMinute);
        tutorClass.setPayForLecture(payForLecture);
        tutorClass.setTimeSlots(new ArrayList<>(timeSlots.stream().sorted().toList()));
        if (learned == 0) tutorClass.setStartDate(startDate);
        return tutorClass;
    }
}
