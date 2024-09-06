package dev.dunglv202.hoaithuong.model.auth;

import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.ConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;

import static dev.dunglv202.hoaithuong.constant.ApiErrorCode.ACCOUNT_LOCKED;
import static dev.dunglv202.hoaithuong.constant.ApiErrorCode.INVALID_USER;

@Component
@RequiredArgsConstructor
public class Oauth2AuthHandler implements AuthenticationSuccessHandler {
    private final AuthHelper authHelper;
    private final UserRepository userRepository;
    private final ConfigService configService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws IOException {
        try {
            OidcUser oidcUser = (OidcUser) auth.getPrincipal();
            User user = userRepository.findByEmail(oidcUser.getEmail())
                .orElseThrow(() -> new ClientVisibleException(HttpStatus.UNAUTHORIZED, INVALID_USER, "{user.invalid}"));

            if (user.isLocked()) throw new ClientVisibleException(HttpStatus.UNAUTHORIZED, ACCOUNT_LOCKED, "{account.locked}");

            // store token
            Configuration configuration = configService.getConfigsByUser(user);
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                "google",
                oidcUser.getName()
            );
            configuration.setGoogleAccessToken(authorizedClient.getAccessToken().getTokenValue());
            configuration.setGoogleRefreshToken(Objects.requireNonNull(authorizedClient.getRefreshToken()).getTokenValue());
            configService.saveConfigs(configuration);

            // set auth token for client
            authHelper.addAuthCookies(resp, authHelper.buildAuthResult(user));
            resp.sendRedirect("/");
        } catch (ClientVisibleException e) {
            resp.setStatus(e.getStatus().value());
            resp.sendRedirect("/signin?err=" + e.getCode());
        }
    }
}
