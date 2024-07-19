package dev.dunglv202.hoaithuong.dto;

import java.util.List;

public record NotificationWrapperDTO(int totalUnread, List<NotificationDTO> notifications) {
}
