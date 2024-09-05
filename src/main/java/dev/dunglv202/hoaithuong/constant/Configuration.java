package dev.dunglv202.hoaithuong.constant;

import java.util.List;

public class Configuration {
    public static final int DEFAULT_LECTURE_DURATION_IN_MINUTE = 70;
    public static final int DEFAULT_PAY_FOR_LECTURE = 80_000;
    public static final String TEACHER_CODE = "I26";
    public static List<String> SUPPORTED_IMAGE_TYPES = List.of(
        "image/jpeg", "image/png", "image/webp", "image/gif"
    );
}
