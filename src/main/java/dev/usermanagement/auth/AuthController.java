package dev.usermanagement.auth;

import dev.usermanagement.permission.RoleLookup;
import dev.usermanagement.shared.ApiException;
import dev.usermanagement.shared.CurrentActor;
import dev.usermanagement.user.UserAccountLookup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {
    record LoginRequest(String loginId, String password) {}
    record TokenResponse(String accessToken, String tokenType) {}
    record ChangePasswordRequest(@NotNull String oldPassword, @NotNull String newPassword) {}

    private static final ApiException BAD_CREDENTIALS = new ApiException(HttpStatus.UNAUTHORIZED,
        "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다.");

    private final UserAccountLookup users;
    private final RoleLookup roles;
    private final AuthCredentialRepository credentials;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final RevokedTokenStore revoked;
    private final CurrentActor currentActor;
    private final String dummyHash;

    AuthController(UserAccountLookup users, RoleLookup roles, AuthCredentialRepository credentials,
                   PasswordEncoder encoder, JwtService jwt, RevokedTokenStore revoked, CurrentActor currentActor) {
        this.users = users;
        this.roles = roles;
        this.credentials = credentials;
        this.encoder = encoder;
        this.jwt = jwt;
        this.revoked = revoked;
        this.currentActor = currentActor;
        this.dummyHash = encoder.encode("dummy-password-for-timing");
    }

    @PostMapping("/login")
    TokenResponse login(@RequestBody(required = false) LoginRequest req) {
        if (req == null || blank(req.loginId()) || blank(req.password())) throw BAD_CREDENTIALS;
        var account = users.findByLoginId(req.loginId()).orElse(null);
        var cred = account == null ? null : credentials.findById(account.id()).orElse(null);
        // always run one BCrypt match so unknown users are not distinguishable by timing
        boolean ok = !tooLong(req.password())
            && encoder.matches(req.password(), cred == null ? dummyHash : cred.getPasswordHash());
        if (!ok || cred == null || !"ACTIVE".equals(account.status())) throw BAD_CREDENTIALS;
        return new TokenResponse(jwt.issue(account.id(), roles.currentRoles(account.id())), "Bearer");
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(Authentication auth) {
        currentActor.required();
        Jwt t = (Jwt) auth.getDetails();
        revoked.revoke(t.getId(), t.getExpiresAt());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/change")
    @Transactional
    ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        long userId = currentActor.required().userId();
        var cred = credentials.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        if (tooLong(req.oldPassword()) || !encoder.matches(req.oldPassword(), cred.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OLD_PASSWORD", "기존 비밀번호가 올바르지 않습니다.");
        }
        CredentialRegistrar.validatePolicy(req.newPassword());
        cred.changePassword(encoder.encode(req.newPassword()));
        return ResponseEntity.noContent().build();
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    private static boolean tooLong(String s) { return s.getBytes(StandardCharsets.UTF_8).length > 72; }
}
