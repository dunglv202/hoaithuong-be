package dev.dunglv202.hoaithuong.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatedDetailProfileDTO {
    @Valid
    private ConfigsDTO configs;
}
