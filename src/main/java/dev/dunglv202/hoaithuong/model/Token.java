package dev.dunglv202.hoaithuong.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Token {
    private String value;
    private LocalDateTime expiredAt;
}
