package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.NotificationWrapperDTO;
import dev.dunglv202.hoaithuong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public NotificationWrapperDTO getNotifications(@RequestParam(required = false, defaultValue = "0") int page) {
        return notificationService.getNotifications(page);
    }
}
