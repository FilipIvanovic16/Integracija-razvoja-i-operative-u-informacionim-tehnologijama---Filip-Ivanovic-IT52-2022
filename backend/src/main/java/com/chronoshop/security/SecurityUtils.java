package com.chronoshop.security;

import com.chronoshop.exception.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadRequestException("Nije pronađen autentifikovani korisnik.");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentPrincipal().getId();
    }

    public static boolean isAdmin() {
        return currentPrincipal().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
