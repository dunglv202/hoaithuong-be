package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.*;
import dev.dunglv202.hoaithuong.helper.SheetHelper;
import dev.dunglv202.hoaithuong.service.SpreadsheetService;
import dev.dunglv202.hoaithuong.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final SpreadsheetService spreadsheetService;

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

    @PostMapping("/upload_avatar")
    @PreAuthorize("isAuthenticated()")
    public UpdateAvatarRespDTO uploadAvatar(@RequestPart MultipartFile file) {
        return userService.updateAvatar(file);
    }

    @GetMapping("/spreadsheets/info")
    @PreAuthorize("isAuthenticated()")
    public SpreadsheetInfoDTO getSpreadsheetInfo(@RequestParam String url) {
        return spreadsheetService.getSpreadsheetInfo(SheetHelper.extractSpreadsheetId(url));
    }
}
