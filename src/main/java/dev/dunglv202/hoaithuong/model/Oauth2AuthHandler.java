package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.repository.ConfigurationRepository;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Oauth2AuthHandler implements AuthenticationSuccessHandler {
    private final AuthHelper authHelper;
    private final UserRepository userRepository;
    private final ConfigurationRepository configurationRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws IOException {
        OidcUser oidcUser = (OidcUser) auth.getPrincipal();
        User user = userRepository.findByEmail(oidcUser.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(oidcUser.getEmail()));

        // store token
        Configuration configuration = configurationRepository.findByUser(user)
            .orElseGet(() -> new Configuration(user));
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            "google",
            oidcUser.getName()
        );
        configuration.setGoogleAccessToken(authorizedClient.getAccessToken().getTokenValue());
        configuration.setGoogleRefreshToken(Objects.requireNonNull(authorizedClient.getRefreshToken()).getTokenValue());
        configurationRepository.save(configuration);

        // set auth token for client
        authHelper.addAuthCookies(resp, authHelper.buildAuthResult(user));
        resp.sendRedirect("/");
    }
}
