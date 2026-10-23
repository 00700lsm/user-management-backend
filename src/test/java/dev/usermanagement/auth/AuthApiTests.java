package dev.usermanagement.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(AuthApiTests.AdminFixture.class)
class AuthApiTests {
    static final String PW = "correct-horse-battery";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CredentialRegistrar registrar;
    @Autowired JwtService jwt;
    @Autowired TransactionTemplate tx;

    @RestController
    static class AdminFixture {
        @GetMapping("/api/admin/_jwt-fixture")
        String admin() { return "ok"; }
    }

    @BeforeEach
    void clean() {
        jdbc.update("delete from auth_credential");
        jdbc.update("delete from user_role");
        jdbc.update("delete from app_user");
    }

    long seed(String loginId, String status, String... roles) {
        jdbc.update("insert into app_user(login_id, email, name, status) values (?,?,?,?)",
            loginId, loginId + "@example.com", loginId, status);
        long id = jdbc.queryForObject("select id from app_user where login_id = ?", Long.class, loginId);
        for (String r : roles) jdbc.update("insert into user_role(user_id, role_code) values (?,?)", id, r);
        registrar.createCredential(id, PW);
        return id;
    }

    ResultActions login(String loginId, String password) throws Exception {
        String body = "{\"loginId\":%s,\"password\":%s}".formatted(q(loginId), q(password));
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    static String q(String s) { return s == null ? "null" : "\"" + s + "\""; }

    String token(String loginId) throws Exception {
        String json = login(loginId, PW).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }

    ResultActions protectedGet(String token) throws Exception {
        var req = get("/api/users/_x");
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req);
    }

    ResultActions admin(String token) throws Exception {
        return mvc.perform(get("/api/admin/_jwt-fixture").header("Authorization", "Bearer " + token));
    }

    ResultActions change(String token, String oldPw, String newPw) throws Exception {
        return mvc.perform(post("/api/auth/password/change").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"oldPassword\":%s,\"newPassword\":%s}".formatted(q(oldPw), q(newPw))));
    }

    // ---- login

    @Test
    void loginIssuesTokenWithExpectedClaims() throws Exception {
        long id = seed("alice", "ACTIVE", "USER", "ADMIN");
        String json = login("alice", PW).andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer")).andReturn().getResponse().getContentAsString();
        Jwt t = jwt.decode(JsonPath.read(json, "$.accessToken"));
        assertEquals(String.valueOf(id), t.getSubject());
        assertEquals(List.of("ADMIN", "USER"), t.getClaimAsStringList("roles"));
        assertEquals("user-management", t.getClaimAsString("iss"));
        assertNotNull(t.getId());
        assertEquals(900, t.getExpiresAt().getEpochSecond() - t.getIssuedAt().getEpochSecond());
        assertNull(t.getAudience());
    }

    @Test
    void everyLoginFailureIsTheSameInvalidCredentials() throws Exception {
        seed("alice", "ACTIVE", "USER");
        seed("bob", "INACTIVE", "USER");
        seed("carol", "WITHDRAWN", "USER");
        String[][] cases = {{"alice", "wrong-password-123"}, {"nobody", PW}, {"bob", PW}, {"carol", PW},
            {"", PW}, {"alice", ""}, {"alice", null}, {null, null}, {"alice", "x".repeat(80)}};
        String message = null;
        for (String[] c : cases) {
            String json = login(c[0], c[1]).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS")).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            String m = JsonPath.read(json, "$.message");
            if (message == null) message = m;
            assertEquals(message, m);
        }
    }

    // ---- protected requests

    @Test
    void protectedRequestWithoutOrWithBadTokenIs401() throws Exception {
        long id = seed("alice", "ACTIVE", "USER");
        String good = token("alice");
        protectedGet(null).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        protectedGet(good.substring(0, good.length() - 2) + "xx").andExpect(status().isUnauthorized());
        protectedGet("garbage").andExpect(status().isUnauthorized());

        Instant now = Instant.now();
        String expired = jwt.sign(claims(id, "user-management", now.minusSeconds(1000), now.minusSeconds(100)));
        protectedGet(expired).andExpect(status().isUnauthorized());
        String wrongIss = jwt.sign(claims(id, "someone-else", now, now.plusSeconds(900)));
        protectedGet(wrongIss).andExpect(status().isUnauthorized());
        // sanity: the same claims with the right issuer are accepted by the filter (404 = past security)
        String ok = jwt.sign(claims(id, "user-management", now, now.plusSeconds(900)));
        protectedGet(ok).andExpect(status().isNotFound());
    }

    JwtClaimsSet claims(long id, String iss, Instant iat, Instant exp) {
        return JwtClaimsSet.builder().issuer(iss).subject(String.valueOf(id)).claim("roles", List.of("USER"))
            .id(java.util.UUID.randomUUID().toString()).issuedAt(iat).expiresAt(exp).build();
    }

    @Test
    void logoutRevokesToken() throws Exception {
        seed("alice", "ACTIVE", "USER");
        String t = token("alice");
        protectedGet(t).andExpect(status().isNotFound());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + t)).andExpect(status().isNoContent());
        protectedGet(t).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void deactivatedUserIsRejectedOnNextRequest() throws Exception {
        long id = seed("alice", "ACTIVE", "USER");
        String t = token("alice");
        jdbc.update("update app_user set status = 'INACTIVE' where id = ?", id);
        protectedGet(t).andExpect(status().isUnauthorized());
    }

    @Test
    void roleChangeIsReflectedOnNextRequest() throws Exception {
        long id = seed("alice", "ACTIVE", "USER");
        String t = token("alice");
        admin(t).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        jdbc.update("insert into user_role(user_id, role_code) values (?, 'ADMIN')", id);
        admin(t).andExpect(status().isOk());
    }

    // ---- change password

    @Test
    void changePasswordRejections() throws Exception {
        seed("alice", "ACTIVE", "USER");
        String t = token("alice");
        change(t, "wrong-old-password", "another-long-password").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_OLD_PASSWORD"));
        change(t, PW, "elevenchars").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("PASSWORD_POLICY_VIOLATION"));
        change(t, PW, "가".repeat(25)).andExpect(status().isBadRequest()) // 75 UTF-8 bytes
            .andExpect(jsonPath("$.code").value("PASSWORD_POLICY_VIOLATION"));
        mvc.perform(post("/api/auth/password/change").header("Authorization", "Bearer " + t)
            .contentType(MediaType.APPLICATION_JSON).content("{\"oldPassword\":\"x\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mvc.perform(post("/api/auth/password/change").header("Authorization", "Bearer " + t)
            .contentType(MediaType.APPLICATION_JSON).content("not json"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        protectedGet(t).andExpect(status().isNotFound()); // token still valid after failures
    }

    @Test
    void changePasswordInvalidatesOldTokensAndNewPasswordWorks() throws Exception {
        seed("alice", "ACTIVE", "USER");
        String old = token("alice");
        Thread.sleep(1100); // iat has second precision; the rule is iat < floor(password_updated_at)
        change(old, PW, "brand-new-password").andExpect(status().isNoContent());
        protectedGet(old).andExpect(status().isUnauthorized());
        login("alice", PW).andExpect(status().isUnauthorized());
        String fresh = login("alice", "brand-new-password").andExpect(status().isOk()).andReturn().getResponse()
            .getContentAsString();
        protectedGet(JsonPath.read(fresh, "$.accessToken")).andExpect(status().isNotFound());
    }

    // ---- registrar / config

    @Test
    void registrarRollsBackWithCallerTransactionOnPolicyViolation() {
        assertThrows(RuntimeException.class, () -> tx.executeWithoutResult(s -> {
            jdbc.update("insert into app_user(login_id, email, name) values ('dave','dave@example.com','dave')");
            long id = jdbc.queryForObject("select id from app_user where login_id = 'dave'", Long.class);
            registrar.createCredential(id, "short");
        }));
        assertEquals(0, jdbc.queryForObject("select count(*) from app_user where login_id = 'dave'", Integer.class));
        assertEquals(0, jdbc.queryForObject("select count(*) from auth_credential", Integer.class));
    }

    @Test
    void shortSecretFailsStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtService("x".repeat(31)));
        assertDoesNotThrow(() -> new JwtService("x".repeat(32)));
    }
}
