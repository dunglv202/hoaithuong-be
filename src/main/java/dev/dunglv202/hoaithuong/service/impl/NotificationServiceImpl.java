package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.NotificationWrapperDTO;
import dev.dunglv202.hoaithuong.entity.Notification;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.mapper.NotificationMapper;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.repository.NotificationRepository;
import dev.dunglv202.hoaithuong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final AuthHelper authHelper;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationWrapperDTO getNotifications(Pagination pagination) {
        User user = authHelper.getSignedUserRef();
        List<Notification> notifications = notificationRepository.findAllByUser(user, pagination.pageable());
        int totalUnread = notificationRepository.countAllUnreadByUser(user);

        return new NotificationWrapperDTO(
            totalUnread,
            notifications.stream().map(NotificationMapper.INSTANCE::toNotificationDTO).toList()
        );
    }

    @Override
    public void addNotification(Notification notification) {
        notificationRepository.save(notification);

        String recipient = notification.getUser().getId().toString();
        messagingTemplate.convertAndSendToUser(
            recipient,
            "/queue/notifications",
            NotificationMapper.INSTANCE.toNotificationDTO(notification)
        );
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = authHelper.getSignedUserRef();
        notificationRepository.markAllAsReadByUser(user);
    }

    @Override
    public void readNotification(long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        if (notification.isRead()) throw new RuntimeException("{notification.already_read}");
        notificationRepository.save(notification.read());
    }
}
