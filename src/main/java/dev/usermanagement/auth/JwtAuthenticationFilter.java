package dev.usermanagement.auth;

import dev.usermanagement.permission.RoleLookup;
import dev.usermanagement.shared.VerifiedActor;
import dev.usermanagement.user.UserAccountLookup;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Verifies the bearer token and re-reads status and roles from the DB on every request.
 * On any failure it leaves the context empty and the entry point answers 401 UNAUTHORIZED
 * (public endpoints such as login stay reachable with a stale token).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final RevokedTokenStore revoked;
    private final UserAccountLookup users;
    private final RoleLookup roles;
    private final AuthCredentialRepository credentials;

    JwtAuthenticationFilter(JwtService jwt, RevokedTokenStore revoked, UserAccountLookup users,
                            RoleLookup roles, AuthCredentialRepository credentials) {
        this.jwt = jwt;
        this.revoked = revoked;
        this.users = users;
        this.roles = roles;
        this.credentials = credentials;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authenticate(header.substring(7).trim()).ifPresent(a ->
                SecurityContextHolder.getContext().setAuthentication(a));
        }
        chain.doFilter(req, res);
    }

    private Optional<UsernamePasswordAuthenticationToken> authenticate(String token) {
        try {
            Jwt t = jwt.decode(token);
            long userId = Long.parseLong(t.getSubject());
            if (t.getId() == null || revoked.isRevoked(t.getId())) return Optional.empty();
            if (!users.findStatus(userId).filter("ACTIVE"::equals).isPresent()) return Optional.empty();
            var cred = credentials.findById(userId);
            // pwv must equal the credential version (password_updated_at millis) stored now
            if (cred.isEmpty() || !(t.getClaims().get("pwv") instanceof Number pwv)
                    || pwv.longValue() != cred.get().getPasswordUpdatedAtMillis()) {
                return Optional.empty();
            }
            Set<String> current = roles.currentRoles(userId);
            var actor = new VerifiedActor(userId, current);
            var auth = new UsernamePasswordAuthenticationToken(actor, null,
                current.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
            auth.setDetails(t); // logout needs jti / exp
            return Optional.of(auth);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    @Configuration
    static class Config {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwt, RevokedTokenStore revoked,
                UserAccountLookup users, RoleLookup roles, AuthCredentialRepository credentials) {
            return new JwtAuthenticationFilter(jwt, revoked, users, roles, credentials);
        }

        // only run inside the security chain, not as a plain servlet filter too
        @Bean
        FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter f) {
            var r = new FilterRegistrationBean<>(f);
            r.setEnabled(false);
            return r;
        }
    }
}
