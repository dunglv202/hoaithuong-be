package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleClassDTO {
    private long id;
    private String code;
    private MinimalStudentDTO student;
}
