package dev.usermanagement.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentActorTests {
    private final CurrentActor resolver = new CurrentActor();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testOnlyUserFixtureProvidesStableUserId() {
        SecurityContextHolder.getContext().setAuthentication(TestActors.activeUser());
        VerifiedActor actor = resolver.required();
        assertEquals(1001L, actor.userId());
        assertEquals(Set.of("USER"), actor.currentRoleCodes());
        assertFalse(actor.hasRole("ADMIN"));
    }

    @Test
    void testOnlyAdminFixtureHasAdminAuthority() {
        SecurityContextHolder.getContext().setAuthentication(TestActors.activeAdmin());
        VerifiedActor actor = resolver.required();
        assertTrue(actor.hasRole("ADMIN"));
    }

    @Test
    void missingAuthenticationFailsClosed() {
        assertThrows(IllegalStateException.class, resolver::required);
    }

    @Test
    void arbitraryAuthenticatedUsernameCannotBecomeVerifiedActor() {
        var forged = new UsernamePasswordAuthenticationToken(
            "1001", "not-a-real-token",
            java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(forged);
        assertThrows(IllegalStateException.class, resolver::required);
    }

    @Test
    void anonymousPrincipalCannotBecomeVerifiedActor() {
        var anon = new AnonymousAuthenticationToken(
            "test-only-key", "anonymousUser",
            java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anon);
        assertThrows(IllegalStateException.class, resolver::required);
    }

    @Test
    void invalidIdentityAndUnknownRolesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new VerifiedActor(0L, Set.of("USER")));
        assertThrows(IllegalArgumentException.class, () -> new VerifiedActor(1001L, Set.of("OWNER")));
        assertThrows(IllegalArgumentException.class, () -> new VerifiedActor(1001L, Set.of()));
    }

    @Test
    void rolesCannotBeMutatedAfterCreation() {
        VerifiedActor actor = new VerifiedActor(1001L, Set.of("USER"));
        assertThrows(UnsupportedOperationException.class, () -> actor.currentRoleCodes().add("ADMIN"));
    }
}
