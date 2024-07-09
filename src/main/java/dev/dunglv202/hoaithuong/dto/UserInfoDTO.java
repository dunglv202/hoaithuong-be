package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.User;
import lombok.Getter;

@Getter
public class UserInfoDTO {
    private final String displayName;
    private final String avatar;

    public UserInfoDTO(User signedUser) {
        this.displayName = signedUser.getDisplayName();
        this.avatar = signedUser.getAvatar();
    }
}
