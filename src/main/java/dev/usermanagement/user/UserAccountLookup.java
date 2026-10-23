package dev.usermanagement.user;

import java.util.Optional;

/**
 * Read-only account lookup used by Auth.
 * Owner: B (User) / C (Permission). Draft proposed by A (Auth); requires owner agreement.
 */
public interface UserAccountLookup {
    Optional<UserAccount> findByLoginId(String loginId);

    /** Current status: ACTIVE / INACTIVE / WITHDRAWN. */
    Optional<String> findStatus(long userId);
}
