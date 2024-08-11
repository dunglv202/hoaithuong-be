package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NotificationWrapperDTO;
import dev.dunglv202.hoaithuong.entity.Notification;
import dev.dunglv202.hoaithuong.model.Pagination;

public interface NotificationService {
    NotificationWrapperDTO getNotifications(Pagination pagination);

    void addNotification(Notification notification);

    void markAllAsRead();

    void readNotification(long id);
}
