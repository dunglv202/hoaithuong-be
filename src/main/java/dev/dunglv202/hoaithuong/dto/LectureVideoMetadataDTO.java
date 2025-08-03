package dev.dunglv202.hoaithuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureVideoMetadataDTO {
    private String title;
    private String description;
    private String thumbnailUrl;

    public static LectureVideoMetadataDTO empty() {
        return LectureVideoMetadataDTO.builder().build();
    }
}
