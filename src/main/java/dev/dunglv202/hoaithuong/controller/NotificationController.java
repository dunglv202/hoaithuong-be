package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.NotificationWrapperDTO;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationWrapperDTO getNotifications(Pagination pagination) {
        return notificationService.getNotifications(pagination.limit(20));
    }

    @PostMapping("/read_all")
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public void readNotification(@PathVariable long id) {
        notificationService.readNotification(id);
    }
}
