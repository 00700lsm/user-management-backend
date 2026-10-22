package dev.usermanagement.shared;

import java.util.Set;

/**
 * Authentication identity verified by the Auth boundary.
 *
 * A production authenticator MUST verify JWT signature/expiry/jti and fetch the
 * ACTIVE User and current Role codes from DB for every protected API call
 * before creating this object. The JWT roles claim alone is never authority.
 *
 * The record is not a token parser. No production implementation creates it yet.
 */
public record VerifiedActor(Long userId, Set<String> currentRoleCodes) {
    public VerifiedActor {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        if (currentRoleCodes == null || currentRoleCodes.isEmpty()) {
            throw new IllegalArgumentException("At least one DB-verified role is required");
        }
        if (!currentRoleCodes.stream().allMatch(r -> "USER".equals(r) || "ADMIN".equals(r))) {
            throw new IllegalArgumentException("Unexpected role code");
        }
        currentRoleCodes = Set.copyOf(currentRoleCodes);
    }

    public boolean hasRole(String roleCode) {
        return currentRoleCodes.contains(roleCode);
    }
}
