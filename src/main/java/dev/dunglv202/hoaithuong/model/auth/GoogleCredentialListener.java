package dev.dunglv202.hoaithuong.model.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class GoogleCredentialListener implements CredentialRefreshListener {
    private final ConfigService configService;
    private final User user;

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        log.info("Refreshed google credential for {}. Storing new credential...", user);
        Configuration updatedConfig = configService.getConfigsByUser(user)
            .setGoogleAccessToken(credential.getAccessToken())
            .setGoogleRefreshToken(credential.getRefreshToken());
        configService.saveConfigs(updatedConfig);
        log.info("Stored new google credential for for {}", user);
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
        log.info("Refresh google credential failed for {}: {}", user, tokenErrorResponse);
    }
}
