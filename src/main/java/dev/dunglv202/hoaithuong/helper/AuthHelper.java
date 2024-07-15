package dev.dunglv202.hoaithuong.helper;

import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.AppUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
            return null;
        }
        return ((AppUser) authentication.getPrincipal()).getUser();
    }

    public User getSignedUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("Could not get signed user");
        }
        return user;
    }
}
