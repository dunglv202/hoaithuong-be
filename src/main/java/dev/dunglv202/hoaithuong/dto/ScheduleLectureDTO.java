package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleLectureDTO {
    private long id;
    private String topic;
    private String comment;
    private String notes;
}
