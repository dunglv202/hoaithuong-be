package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentDTO {
    private Long id;
    private String name;
    private PersonDTO reportTo;
    private String notes;
    private boolean active;
}
