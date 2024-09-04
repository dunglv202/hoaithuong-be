package dev.dunglv202.hoaithuong.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewScheduleDTO {
    @NotNull(message = "{lecture.class.required}")
    private Long classId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "{schedule.start_time.required}")
    private LocalDateTime startTime;
}
