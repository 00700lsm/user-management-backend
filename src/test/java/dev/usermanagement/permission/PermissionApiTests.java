package dev.usermanagement.permission;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import dev.usermanagement.auth.CredentialRegistrar;
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

/** TC-PERM-001, TC-PERM-002, and DEC-009 role refresh on the next protected call. */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PermissionApiTests {
    static final String PW = "correct-horse-battery";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CredentialRegistrar registrar;

    @BeforeEach
    void clean() {
        jdbc.update("delete from auth_credential");
        jdbc.update("delete from user_role");
        jdbc.update("delete from app_user");
    }

    long user(String loginId, String status, String... roles) {
        jdbc.update("insert into app_user(login_id, email, name, status) values (?,?,?,?)",
            loginId, loginId + "@example.com", loginId, status);
        long id = jdbc.queryForObject("select id from app_user where login_id = ?", Long.class, loginId);
        for (String role : roles) jdbc.update("insert into user_role(user_id, role_code) values (?,?)", id, role);
        return id;
    }

    String token(String loginId) throws Exception {
        registrar.createCredential(
            jdbc.queryForObject("select id from app_user where login_id = ?", Long.class, loginId), PW);
        String json = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + loginId + "\",\"password\":\"" + PW + "\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }

    ResultActions roles(String token) throws Exception {
        var request = get("/api/admin/roles");
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }

    ResultActions userRoles(String token, long id) throws Exception {
        return mvc.perform(get("/api/admin/users/" + id + "/roles").header("Authorization", "Bearer " + token));
    }

    ResultActions replace(String token, long id, String json) throws Exception {
        var request = put("/api/admin/users/" + id + "/roles").contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }

    @Test
    void adminListsSeededRoles() throws Exception {
        user("admin", "ACTIVE", "ADMIN");
        String admin = token("admin");
        roles(admin).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].code").value("ADMIN"))
            .andExpect(jsonPath("$[0].name").value("Administrator"))
            .andExpect(jsonPath("$[1].code").value("USER"))
            .andExpect(jsonPath("$[1].name").value("General user"));
    }

    @Test
    void roleListRejectsGuestsAndUsers() throws Exception {
        roles(null).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        user("alice", "ACTIVE", "USER");
        roles(token("alice")).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminReadsAndReplacesRoles() throws Exception {
        user("admin", "ACTIVE", "ADMIN");
        long target = user("alice", "ACTIVE", "USER");
        String admin = token("admin");

        userRoles(admin, target).andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(target))
            .andExpect(jsonPath("$.roleCodes.length()").value(1))
            .andExpect(jsonPath("$.roleCodes[0]").value("USER"));

        replace(admin, target, "{\"roleCodes\":[\"USER\",\"ADMIN\"]}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roleCodes.length()").value(2))
            .andExpect(jsonPath("$.roleCodes[0]").value("ADMIN"))
            .andExpect(jsonPath("$.roleCodes[1]").value("USER"));

        userRoles(admin, target).andExpect(jsonPath("$.roleCodes[0]").value("ADMIN"))
            .andExpect(jsonPath("$.roleCodes[1]").value("USER"));

        replace(admin, target, "{\"roleCodes\":[\"USER\"]}").andExpect(status().isOk())
            .andExpect(jsonPath("$.roleCodes.length()").value(1))
            .andExpect(jsonPath("$.roleCodes[0]").value("USER"));
    }

    @Test
    void missingUserIsNotFoundAndInvalidBodiesDoNotChangeRoles() throws Exception {
        user("admin", "ACTIVE", "ADMIN");
        long target = user("alice", "ACTIVE", "USER");
        String admin = token("admin");

        userRoles(admin, 999_999L).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        replace(admin, 999_999L, "{\"roleCodes\":[\"USER\"]}")
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));

        replace(admin, target, "{\"roleCodes\":[]}").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        replace(admin, target, "{\"roleCodes\":[\"USER\",\"USER\"]}").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        replace(admin, target, "{\"roleCodes\":[\"OWNER\"]}").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        replace(admin, target, "not json").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        userRoles(admin, target).andExpect(jsonPath("$.roleCodes.length()").value(1))
            .andExpect(jsonPath("$.roleCodes[0]").value("USER"));
    }

    @Test
    void userCannotReadOrReplaceRoles() throws Exception {
        long target = user("alice", "ACTIVE", "USER");
        user("bob", "ACTIVE", "USER");
        String alice = token("alice");
        userRoles(alice, target).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        replace(alice, target, "{\"roleCodes\":[\"ADMIN\"]}")
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        replace(null, target, "{\"roleCodes\":[\"ADMIN\"]}")
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void roleChangeAppliesOnTheNextRequestWithoutRevokingTheToken() throws Exception {
        long aliceId = user("alice", "ACTIVE", "USER");
        user("admin", "ACTIVE", "ADMIN");
        String alice = token("alice");
        String admin = token("admin");

        roles(alice).andExpect(status().isForbidden());
        replace(admin, aliceId, "{\"roleCodes\":[\"ADMIN\"]}").andExpect(status().isOk());
        roles(alice).andExpect(status().isOk());

        replace(admin, aliceId, "{\"roleCodes\":[\"USER\"]}").andExpect(status().isOk());
        roles(alice).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void inactiveAndWithdrawnUsersCanStillHaveRolesRead() throws Exception {
        user("admin", "ACTIVE", "ADMIN");
        long inactive = user("bob", "INACTIVE", "USER");
        long withdrawn = user("carol", "WITHDRAWN", "ADMIN");
        String admin = token("admin");
        userRoles(admin, inactive).andExpect(status().isOk()).andExpect(jsonPath("$.roleCodes[0]").value("USER"));
        userRoles(admin, withdrawn).andExpect(status().isOk()).andExpect(jsonPath("$.roleCodes[0]").value("ADMIN"));
    }

    @Test
    void userWithNoRolesReturnsAnEmptyList() throws Exception {
        user("admin", "ACTIVE", "ADMIN");
        long target = user("alice", "ACTIVE");
        userRoles(token("admin"), target).andExpect(status().isOk())
            .andExpect(jsonPath("$.roleCodes.length()").value(0));
    }
}
