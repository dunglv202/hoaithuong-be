package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.DetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedDetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UserInfoDTO;
import dev.dunglv202.hoaithuong.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UserInfoDTO getUserInfo() {
        return userService.getSignedUserInfo();
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public DetailProfileDTO getDetailProfile() {
        return userService.getDetailProfile();
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public void updateDetailProfile(@Valid @RequestBody UpdatedDetailProfileDTO updateDTO) {
        userService.updateDetailProfile(updateDTO);
    }
}
