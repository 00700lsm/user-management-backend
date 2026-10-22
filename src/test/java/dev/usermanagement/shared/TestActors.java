package dev.usermanagement.shared;

import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * ISOLATED TEST-ONLY fixture. Never import this into src/main or production.
 * It does not sign JWTs and must NOT be presented as a G3 auth test.
 */
public final class TestActors {
    private TestActors() {}

    public static Authentication activeUser() {
        return actor(1001L, Set.of("USER"));
    }

    public static Authentication activeAdmin() {
        return actor(1002L, Set.of("USER", "ADMIN"));
    }

    public static Authentication actor(long id, Set<String> databaseRoles) {
        VerifiedActor principal = new VerifiedActor(id, databaseRoles);
        return new UsernamePasswordAuthenticationToken(
            principal, null,
            databaseRoles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList()
        );
    }
}
