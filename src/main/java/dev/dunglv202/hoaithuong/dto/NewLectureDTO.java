package dev.dunglv202.hoaithuong.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Setter
@Getter
public class NewLectureDTO {
    @NotNull(message = "{lecture.class.required}")
    private Long classId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotBlank(message = "{lecture.topic.required}")
    @Length(max = 128, message = "{lecture.topic.length}")
    private String topic;

    @Length(max = 512, message = "{lecture.video.url.length}")
    private String video;

    @Length(max = 256, message = "{lecture.comment.length}")
    private String comment;

    @Length(max = 256, message = "{lecture.notes.length}")
    private String notes;

    private Long scheduleId;
}
