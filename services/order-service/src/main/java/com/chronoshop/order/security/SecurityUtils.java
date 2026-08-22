package com.chronoshop.order.security;

import com.chronoshop.exception.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Principal ovde je userId (String) koji je gateway-header filter upisao kao "username" u
 * UsernamePasswordAuthenticationToken - nema pravog UserDetails objekta kao u auth-service, jer
 * order-service ne drzi korisnicke podatke lokalno.
 */
public final class SecurityUtils {

  private SecurityUtils() {}

  public static Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      throw new BadRequestException("Nije pronađen autentifikovani korisnik.");
    }
    try {
      return Long.parseLong(auth.getName());
    } catch (NumberFormatException e) {
      throw new BadRequestException("Nevalidan identifikator korisnika.");
    }
  }

  public static boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null
        && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }
}
