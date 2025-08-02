package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.constant.TutorClassType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleClassDTO {
    private long id;
    private TutorClassType type;
    private String code;
    private MinimalStudentDTO student;
}
