package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.NotificationDTO;
import dev.dunglv202.hoaithuong.dto.NotificationWrapperDTO;
import dev.dunglv202.hoaithuong.entity.Notification;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.repository.NotificationRepository;
import dev.dunglv202.hoaithuong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final AuthHelper authHelper;
    private final NotificationRepository notificationRepository;

    @Override
    public NotificationWrapperDTO getNotifications(int page) {
        User user = authHelper.getSignedUser();
        List<Notification> notifications = notificationRepository.findAllByUser(user, PageRequest.of(page, 10));
        int totalUnread = notificationRepository.countAllUnreadByUser(user);

        return new NotificationWrapperDTO(
            totalUnread,
            notifications.stream().map(NotificationDTO::new).toList()
        );
    }
}
