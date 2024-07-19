package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NotificationWrapperDTO;

public interface NotificationService {
    NotificationWrapperDTO getNotifications(int page);
}
