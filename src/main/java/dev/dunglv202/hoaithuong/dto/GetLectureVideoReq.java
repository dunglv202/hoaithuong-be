package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.validator.ValidGetVideoRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Getter
@Setter
@ValidGetVideoRequest
public class GetLectureVideoReq {
    @Nullable
    private String classCode;
    @Nullable
    private String classUid;
    @Nullable
    private Integer lecture;
    @Nullable
    private LocalDateTime timestamp;
}
