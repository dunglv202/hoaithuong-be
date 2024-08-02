package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.model.TimeSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class UpdatedTutorClassDTO {
    private long id;

    @NotBlank(message = "{tutor_class.code.not_blank}")
    private String code;

    @NotNull(message = "{tutor_class.level.required}")
    private String level;

    @Length(max = 256, message = "{tutor_class.notes.length}")
    private String notes;

    @NotNull(message = "{tutor_class.timeslots.required}")
    @Size(min = 1, message = "{tutor_class.timeslots.required}")
    private Set<@Valid TimeSlot> timeSlots;

    private LocalDate startDate = LocalDate.now();

    @NotNull
    @PositiveOrZero(message = "{tutor_class.pay.positive}")
    private Integer payForLecture;
}
