package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Lecture;
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

    @NotNull(message = "{lecture.start_time.required}")
    private LocalDateTime startTime;

    @NotNull(message = "{lecture.end_time.required}")
    private LocalDateTime endTime;

    @NotBlank(message = "{lecture.topic.required}")
    private String topic;

    @Length(max = 256, message = "{lecture.notes.length}")
    private String notes;

    public Lecture toEntity() {
        Lecture lecture = new Lecture();
        lecture.setStartTime(startTime);
        lecture.setEndTime(endTime);
        lecture.setTopic(topic);
        lecture.setNotes(notes);
        return lecture;
    }
}
