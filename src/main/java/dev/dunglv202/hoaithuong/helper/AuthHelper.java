package dev.dunglv202.hoaithuong.helper;

import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.AppUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {
    public User getSignedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("Could not get user from anonymous session");
        }

        return ((AppUser) authentication.getPrincipal()).getUser();
    }
}
