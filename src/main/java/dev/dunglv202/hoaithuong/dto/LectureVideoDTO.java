package dev.dunglv202.hoaithuong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
@Builder
public class LectureVideoDTO {
    @Nullable
    private String url;

    @JsonProperty("isIframe")
    private boolean isIframe;
}
