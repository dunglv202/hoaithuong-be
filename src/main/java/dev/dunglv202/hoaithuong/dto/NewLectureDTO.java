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

    private LocalDateTime startTime;

    @NotBlank(message = "{lecture.topic.required}")
    private String topic;

    @Length(max = 256, message = "{lecture.comment.length}")
    private String comment;

    @Length(max = 256, message = "{lecture.notes.length}")
    private String notes;

    private Long scheduleId;

    public Lecture toEntity() {
        Lecture lecture = new Lecture();
        lecture.setTopic(topic);
        lecture.setComment(comment);
        lecture.setNotes(notes);
        return lecture;
    }
}
