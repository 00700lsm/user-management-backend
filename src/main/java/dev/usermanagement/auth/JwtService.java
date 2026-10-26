package dev.usermanagement.auth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

/** HS256 access tokens, 15 minutes, no refresh. */
@Component
public class JwtService {
    static final String ISSUER = "user-management";
    static final Duration TTL = Duration.ofMinutes(15);

    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;

    JwtService(@Value("${auth.jwt.secret}") String secret) {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        if (key.length < 32) {
            throw new IllegalStateException("auth.jwt.secret (JWT_SECRET) must be at least 32 bytes");
        }
        var hmac = new SecretKeySpec(key, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(hmac));
        this.decoder = NimbusJwtDecoder.withSecretKey(hmac).macAlgorithm(MacAlgorithm.HS256).build();
        // skew 0 (the default validator allows 60s)
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(ISSUER)));
    }

    /** pwv = auth_credential.password_updated_at epoch millis; changes on every password change. */
    String issue(long userId, Set<String> roles, long pwv) {
        Instant now = Instant.now();
        return sign(JwtClaimsSet.builder().issuer(ISSUER).subject(String.valueOf(userId))
            .claim("roles", roles.stream().sorted().toList()) .claim("pwv", pwv).id(UUID.randomUUID().toString())
            .issuedAt(now).expiresAt(now.plus(TTL)).build());
    }

    /** Signs arbitrary claims; also lets tests forge expired / wrong-issuer tokens. */
    public String sign(JwtClaimsSet claims) {
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    }

    /** Throws JwtException on bad signature, expiry or issuer. */
    Jwt decode(String token) { return decoder.decode(token); }
}
