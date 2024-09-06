package dev.dunglv202.hoaithuong.model;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * Notes: Set user before each usage. This listener will reset user to null after processing to avoid mis-updating
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleCredentialListener implements CredentialRefreshListener {
    private User user;

    private final ConfigService configService;

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        log.info("Refreshed google credential for {}. Storing new credential...", user);
        Configuration updatedConfig = configService.getConfigsByUser(user)
            .setGoogleAccessToken(credential.getAccessToken())
            .setGoogleRefreshToken(credential.getRefreshToken());
        configService.saveConfigs(updatedConfig);
        log.info("Stored new google credential for for {}", user);
        this.user = null;
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
        log.info(
            "Refresh google credential failed for {}: {} - {}",
            user,
            tokenErrorResponse.getErrorDescription(),
            tokenErrorResponse.getError()
        );
        this.user = null;
    }

    public GoogleCredentialListener setUser(User user) {
        this.user = user;
        return this;
    }
}
