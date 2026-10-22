package dev.usermanagement.shared;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Shared access point for the Auth/User/Permission modules.
 * Only the future production Auth verifier may place a VerifiedActor in the
 * SecurityContext after JWT verification AND current DB status/Role checks.
 *
 * Never recover identity from request parameters, headers or raw JWT claims.
 */
@Component
public class CurrentActor {
    public VerifiedActor required() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Authenticated actor is unavailable");
        }
        if (!(auth.getPrincipal() instanceof VerifiedActor verified)) {
            throw new IllegalStateException("Security principal is not a DB-verified actor");
        }
        return verified;
    }
}
