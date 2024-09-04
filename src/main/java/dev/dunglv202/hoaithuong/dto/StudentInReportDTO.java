package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentInReportDTO {
    private long id;
    private String name;
    private PersonDTO reportTo;
}
