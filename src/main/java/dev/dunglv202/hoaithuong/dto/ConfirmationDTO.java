package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.validator.Image;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ConfirmationDTO {
    private long studentId;

    @Image
    private MultipartFile confirmation;
}
