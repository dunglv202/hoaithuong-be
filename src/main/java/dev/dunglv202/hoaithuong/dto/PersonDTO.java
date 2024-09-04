package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.constant.Salutation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonDTO {
    @NotNull(message = "{person.salutation.required}")
    private Salutation salutation;

    @NotBlank(message = "{person.name.required}")
    private String name;
}
