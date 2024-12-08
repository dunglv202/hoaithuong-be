package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StudentInReportDTO {
    private long id;
    private String name;
    private PersonDTO reportTo;
    private String confirmationUrl;
    private List<LectureInReportDTO> lectures;
}
