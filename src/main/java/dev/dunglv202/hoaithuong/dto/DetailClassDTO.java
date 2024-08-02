package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.model.TimeSlot;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DetailClassDTO {
    private long id;
    private String code;
    private StudentDTO student;
    private String level;
    private String notes;
    private int totalLecture;
    private int learned;
    private int durationInMinute;
    private int payForLecture;
    private List<TimeSlot> timeSlots;
}
