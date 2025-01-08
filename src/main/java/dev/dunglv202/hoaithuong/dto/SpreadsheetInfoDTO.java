package dev.dunglv202.hoaithuong.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SpreadsheetInfoDTO {
    private String id;
    private String name;
    private List<String> sheets;
}
