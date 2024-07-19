package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Notification;
import lombok.Getter;

import java.time.Instant;

@Getter
public class NotificationDTO {
    private final long id;
    private final String content;
    private final boolean read;
    private final Instant timestamp;

    public NotificationDTO(Notification notification) {
        id = notification.getId();
        content = notification.getContent();
        read = notification.isRead();
        timestamp = notification.getTimestamp();
    }
}
