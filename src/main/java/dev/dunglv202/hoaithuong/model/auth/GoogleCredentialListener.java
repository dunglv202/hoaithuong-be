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
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class GoogleCredentialListener implements CredentialRefreshListener {
    private final TransactionTemplate transactionTemplate;
    private final ConfigService configService;
    private final User user;

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        log.info("Refreshed google credential for {}. Storing new credential...", user);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                Configuration updatedConfig = configService.getConfigsByUser(user)
                    .setGoogleAccessToken(credential.getAccessToken())
                    .setGoogleRefreshToken(credential.getRefreshToken());
                configService.saveConfigs(updatedConfig);
                log.info("Stored new google credential for for {}", user);
            }
        });
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
        log.info("Refresh google credential failed for {}: {}", user, tokenErrorResponse);
    }
}
