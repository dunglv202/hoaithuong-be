package dev.dunglv202.hoaithuong.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class Token {
    private String value;
    private Instant expiredAt;
}
