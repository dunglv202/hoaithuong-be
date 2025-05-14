package dev.dunglv202.hoaithuong.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
@Builder
public class LectureVideoDTO {
    @Nullable
    private String url;

    private boolean isIframe;
}
