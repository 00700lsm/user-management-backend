package dev.usermanagement.permission;

import java.util.Set;

/**
 * Read-only current role codes used by Auth.
 * Owner: B (User) / C (Permission). Draft proposed by A (Auth); requires owner agreement.
 */
public interface RoleLookup {
    Set<String> currentRoles(long userId);
}
