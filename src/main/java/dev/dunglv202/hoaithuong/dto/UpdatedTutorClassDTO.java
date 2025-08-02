package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.model.TimeSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class UpdatedTutorClassDTO {
    private long id;

    private String code;

    private String level;

    @Length(max = 256, message = "{tutor_class.notes.length}")
    private String notes;

    private Set<@Valid TimeSlot> timeSlots = Set.of();

    /**
     * Date that changes will be applied
     */
    private LocalDate startDate = LocalDate.now();

    @NotNull
    @PositiveOrZero(message = "{tutor_class.pay.positive}")
    private Integer payForLecture;
}
