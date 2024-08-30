package dev.dunglv202.hoaithuong.helper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.repository.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class GoogleHelper {
    public static HttpTransport HTTP_TRANSPORT;
    public static JsonFactory JSON_FACTORY;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final ConfigurationRepository configurationRepository;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            JSON_FACTORY = GsonFactory.getDefaultInstance();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public Credential getCredential(User user) {
        Configuration config = configurationRepository.findByUser(user)
            .orElse(new Configuration(user));

        return new GoogleCredential.Builder()
            .setTransport(HTTP_TRANSPORT)
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(clientId, clientSecret)
            .build()
            .setAccessToken(config.getGoogleAccessToken())
            .setRefreshToken(config.getGoogleRefreshToken());
    }
}
