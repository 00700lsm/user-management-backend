package dev.usermanagement.auth;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "auth_credential")
public class AuthCredentialEntity {
    @Id @Column(name = "user_id")
    private Long userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "password_updated_at", nullable = false)
    private OffsetDateTime passwordUpdatedAt;

    protected AuthCredentialEntity() {}

    AuthCredentialEntity(Long userId, String passwordHash) {
        this.userId = userId;
        changePassword(passwordHash);
    }

    String getPasswordHash() { return passwordHash; }
    long getPasswordUpdatedAtMillis() { return passwordUpdatedAt.toInstant().toEpochMilli(); }

    void changePassword(String newHash) {
        this.passwordHash = newHash;
        // millisecond precision (matches the pwv claim) and strictly after the previous value
        var now = OffsetDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        if (passwordUpdatedAt != null && !now.isAfter(passwordUpdatedAt)) now = passwordUpdatedAt.plusNanos(1_000_000);
        this.passwordUpdatedAt = now;
    }
}
