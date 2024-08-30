package dev.dunglv202.hoaithuong.helper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.AuthenticationException;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static dev.dunglv202.hoaithuong.constant.ApiErrorCode.REQUIRE_GOOGLE_AUTH;

@Component
@RequiredArgsConstructor
public class GoogleHelper {
    public static HttpTransport HTTP_TRANSPORT;
    public static JsonFactory JSON_FACTORY;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.application.name}")
    private String applicationName;

    private final ConfigService configService;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            JSON_FACTORY = GsonFactory.getDefaultInstance();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Sheets getSheetService(User user) {
        Credential credential = getCredential(user);
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(applicationName)
            .build();
    }

    @SuppressWarnings("deprecation")
    public Credential getCredential(User user) {
        Configuration config = configService.getConfigsByUser(user);

        if (config.getGoogleAccessToken() == null) {
            throw new AuthenticationException(REQUIRE_GOOGLE_AUTH);
        }

        return new GoogleCredential.Builder()
            .setTransport(HTTP_TRANSPORT)
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(clientId, clientSecret)
            .build()
            .setAccessToken(config.getGoogleAccessToken())
            .setRefreshToken(config.getGoogleRefreshToken());
    }
}
