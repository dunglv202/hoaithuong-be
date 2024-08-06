package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UpdatedLecture {
    private long id;

    @Length(max = 256, message = "{lecture.comment.length}")
    private String comment;

    @Length(max = 256, message = "{lecture.notes.length}")
    private String notes;
}
