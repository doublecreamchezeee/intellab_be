package com.example.identityservice.utility;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class AuthenticatedUserIdProvider {
    public String getUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElseThrow(IllegalStateException::new);
    }

}