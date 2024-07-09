package dev.dunglv202.hoaithuong.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResult {
    private Token accessToken;
    private Token refreshToken;
}
