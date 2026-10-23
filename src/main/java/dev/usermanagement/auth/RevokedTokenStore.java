package dev.usermanagement.auth;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class RevokedTokenStore {
    // PoC limit: in memory, lost on restart (revoked tokens may be reused until exp, max 15 min).
    private final ConcurrentHashMap<String, Instant> revoked = new ConcurrentHashMap<>();

    void revoke(String jti, Instant expiresAt) {
        Instant now = Instant.now();
        revoked.values().removeIf(exp -> !exp.isAfter(now));
        revoked.put(jti, expiresAt);
    }

    boolean isRevoked(String jti) { return revoked.containsKey(jti); }
}
