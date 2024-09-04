package dev.dunglv202.hoaithuong.model.auth;

import dev.dunglv202.hoaithuong.model.Token;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResult {
    private Token accessToken;
    private Token refreshToken;
}
