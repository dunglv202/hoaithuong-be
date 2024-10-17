package dev.dunglv202.hoaithuong.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UpdatedLecture {
    private long id;

    @NotBlank(message = "{lecture.topic.required}")
    @Length(max = 128, message = "{lecture.topic.length}")
    private String topic;

    @Length(max = 256, message = "{lecture.video.url.length}")
    private String video;

    @Length(max = 256, message = "{lecture.comment.length}")
    private String comment;

    @Length(max = 256, message = "{lecture.notes.length}")
    private String notes;
}
