package dev.usermanagement.auth;

import dev.usermanagement.shared.ApiException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Called by User signup inside its transaction; a policy violation rolls the whole signup back. */
@Service
public class CredentialRegistrar {
    private final AuthCredentialRepository repository;
    private final PasswordEncoder encoder;

    CredentialRegistrar(AuthCredentialRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void createCredential(long userId, String rawPassword) {
        validatePolicy(rawPassword);
        repository.saveAndFlush(new AuthCredentialEntity(userId, encoder.encode(rawPassword)));
    }

    /** At least 12 characters and at most 72 UTF-8 bytes (BCrypt limit). */
    static void validatePolicy(String raw) {
        if (raw == null || raw.codePointCount(0, raw.length()) < 12
                || raw.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_POLICY_VIOLATION",
                "비밀번호는 12자 이상, UTF-8 72바이트 이하여야 합니다.");
        }
    }
}
