package dev.dunglv202.hoaithuong.model;

import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Oauth2AuthHandler implements AuthenticationSuccessHandler {
    private final AuthHelper authHelper;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws IOException {
        OidcUser oidcUser = (OidcUser) auth.getPrincipal();

        User user = userRepository.findByEmail(oidcUser.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(oidcUser.getEmail()));

        authHelper.addAuthCookies(resp, authHelper.buildAuthResult(user));
        resp.sendRedirect("/");
    }
}
