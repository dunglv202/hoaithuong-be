package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Level;
import lombok.Getter;

@Getter
public class LevelDTO {
    private final String code;
    private final String label;

    public LevelDTO(Level level) {
        this.code = level.getCode();
        this.label = level.getLabel();
    }
}
