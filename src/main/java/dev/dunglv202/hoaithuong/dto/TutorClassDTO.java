package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorClassDTO {
    private Long id;
    private String code;
    private StudentDTO student;
    private String level;
    private int totalLecture;
    private Integer learned;
    private String notes;
    private boolean active;
}
