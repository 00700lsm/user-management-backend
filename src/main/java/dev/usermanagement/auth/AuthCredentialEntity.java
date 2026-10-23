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
    OffsetDateTime getPasswordUpdatedAt() { return passwordUpdatedAt; }

    void changePassword(String newHash) {
        this.passwordHash = newHash;
        this.passwordUpdatedAt = OffsetDateTime.now();
    }
}
