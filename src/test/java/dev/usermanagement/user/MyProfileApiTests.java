package dev.usermanagement.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import dev.usermanagement.auth.CredentialRegistrar;
import dev.usermanagement.shared.TestActors;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * TC-USER-002 (조회 부분): GET /api/users/me — REQ-USER-002 / UI-USER-002.
 * Uses a real JWT issued by POST /api/auth/login (Auth #8), not the TestActors fixture.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MyProfileApiTests {
    static final String PW = "correct-horse-battery";
    /** Exactly the properties of the OpenAPI User schema; nothing more may leak. */
    static final Set<String> USER_SCHEMA_FIELDS = Set.of("id", "loginId", "name", "email", "status", "createdAt");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CredentialRegistrar registrar;

    @BeforeEach
    void clean() {
        jdbc.update("delete from auth_credential");
        jdbc.update("delete from user_role");
        jdbc.update("delete from app_user");
    }

    long seed(String loginId, String name) {
        jdbc.update("insert into app_user(login_id, email, name, status) values (?,?,?,'ACTIVE')",
            loginId, loginId + "@example.com", name);
        long id = jdbc.queryForObject("select id from app_user where login_id = ?", Long.class, loginId);
        jdbc.update("insert into user_role(user_id, role_code) values (?, 'USER')", id);
        registrar.createCredential(id, PW);
        return id;
    }

    String token(String loginId) throws Exception {
        String body = "{\"loginId\":\"%s\",\"password\":\"%s\"}".formatted(loginId, PW);
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }

    ResultActions getMe(String token) throws Exception {
        var req = get("/api/users/me");
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req);
    }

    @Test
    void guestGets401() throws Exception {
        getMe(null)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void activeUserGetsOwnProfileInUserSchemaShape() throws Exception {
        long id = seed("alice", "앨리스");

        String json = getMe(token("alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.loginId").value("alice"))
            .andExpect(jsonPath("$.name").value("앨리스"))
            .andExpect(jsonPath("$.email").value("alice@example.com"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn().getResponse().getContentAsString();

        Map<String, Object> body = JsonPath.read(json, "$");
        assertEquals(USER_SCHEMA_FIELDS, body.keySet());
        // OpenAPI format: date-time (RFC 3339 string, not a number)
        assertNotNull(OffsetDateTime.parse((String) body.get("createdAt")));
    }

    @Test
    void identityComesFromTokenNotRequest() throws Exception {
        seed("alice", "앨리스");
        long bobId = seed("bob", "밥");

        // a client-supplied id must not change whose profile is returned
        mvc.perform(get("/api/users/me").param("id", String.valueOf(bobId))
                .header("Authorization", "Bearer " + token("alice")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.loginId").value("alice"));
    }

    @Test
    void deactivatedUserTokenGets401() throws Exception {
        long id = seed("alice", "앨리스");
        String token = token("alice");
        jdbc.update("update app_user set status = 'INACTIVE' where id = ?", id);

        // DEC-005: enforced by the Auth filter before this endpoint runs
        getMe(token).andExpect(status().isUnauthorized());
    }

    @Test
    void verifiedActorWithoutUserRowGets401() throws Exception {
        // Unreachable with a real token (the filter checks the row first); covers the controller's own guard.
        mvc.perform(get("/api/users/me").with(authentication(TestActors.actor(999_999L, Set.of("USER")))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
