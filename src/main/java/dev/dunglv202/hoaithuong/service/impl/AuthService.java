package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.CredentialDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.JwtProvider;
import dev.dunglv202.hoaithuong.model.auth.AppUser;
import dev.dunglv202.hoaithuong.model.auth.AuthResult;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthHelper authHelper;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthResult login(CredentialDTO credential) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    credential.getUsername(),
                    credential.getPassword()
                )
            );
            AppUser user = (AppUser) authentication.getPrincipal();

            // reset login try
            user.getUser().setLoginTry(0);
            userRepository.save(user.getUser());

            return authHelper.buildAuthResult(user.getUser());
        } catch (BadCredentialsException e) {
            userRepository.findByUsernameOrEmail(credential.getUsername()).ifPresent(user -> {
                user.setLoginTry(user.getLoginTry() + 1);
                if (user.getLoginTry() >= MAX_LOGIN_TRY) {
                    user.setLocked(true);
                }
                userRepository.save(user);
            });
            throw new BadCredentialsException("{auth.credentials.incorrect}", e);
        }
    }

    public AuthResult refresh(String refreshToken) {
        Claims claims = jwtProvider.verifyToken(refreshToken);
        User user = userRepository.findById(Long.parseLong(claims.getSubject())).orElseThrow();

        if (!JWT_TOKEN_TYPE_REFRESH.equals(claims.get(JWT_TOKEN_TYPE_KEY))) {
            throw new RuntimeException("{jwt.bad_malformed}");
        }

        return authHelper.buildAuthResult(user);
    }
}
