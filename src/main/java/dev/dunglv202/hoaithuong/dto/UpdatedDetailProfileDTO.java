package dev.dunglv202.hoaithuong.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UpdatedDetailProfileDTO {
    @NotBlank(message = "{user.display_name.required}")
    @Length(max = 20, message = "{user.display_name.most_20_chars}")
    private String displayName;

    @Valid
    @NotNull(message = "{user.configs.required}")
    private ConfigsDTO configs;
}
