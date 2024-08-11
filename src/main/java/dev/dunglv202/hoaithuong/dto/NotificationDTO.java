package dev.dunglv202.hoaithuong.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationDTO {
    private long id;
    private String content;
    private boolean read;
    private Instant timestamp;
}
