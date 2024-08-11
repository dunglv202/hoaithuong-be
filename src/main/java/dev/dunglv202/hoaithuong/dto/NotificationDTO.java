package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.constant.NotiType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationDTO {
    private long id;
    private String content;
    private boolean read;
    private NotiType type;
    private Object payload;
    private boolean resolved;
    private Instant timestamp;
}
