package dev.usermanagement.shared;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * G2 preparation only: demonstrate Spring Security 401/403 with test-only
 * controllers. These paths must never be included in the production artifact.
 * JWT issuance, signature, ACTIVE status and current DB role lookup are NOT
 * exercised and may not be reported as G3 or real operation contract tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(AuthBoundaryMockMvcTests.FixtureController.class)
class AuthBoundaryMockMvcTests {
    @Autowired MockMvc mvc;

    @Test
    void guestCannotAccessProtectedUserEndpoint() throws Exception {
        mvc.perform(get("/api/users/_auth-fixture"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void guestCannotAccessAdminEndpoint() throws Exception {
        mvc.perform(get("/api/admin/_auth-fixture"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void activeUserCanResolveVerifiedIdentity() throws Exception {
        mvc.perform(get("/api/users/_auth-fixture").with(authentication(TestActors.activeUser())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1001))
            .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void userDeniedAdminEndpointWith403() throws Exception {
        mvc.perform(get("/api/admin/_auth-fixture").with(authentication(TestActors.activeUser())))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminAllowedAdminEndpoint() throws Exception {
        mvc.perform(get("/api/admin/_auth-fixture").with(authentication(TestActors.activeAdmin())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1002))
            .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    void arbitraryUsernameWithAdminRoleCannotBeTreatedAsVerifiedActor() throws Exception {
        var unverified = new UsernamePasswordAuthenticationToken(
            "1001", "not-a-jwt",
            java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        // SecurityFilterChain can authorize based on the supplied test authority,
        // but the application boundary must never trust an unknown principal type.
        mvc.perform(get("/api/admin/_auth-fixture").with(authentication(unverified)))
            .andExpect(status().is5xxServerError());
    }

    @RestController
    static class FixtureController {
        private final CurrentActor currentActor;

        FixtureController(CurrentActor currentActor) {
            this.currentActor = currentActor;
        }

        @GetMapping("/api/users/_auth-fixture")
        ResponseEntity<Map<String, Object>> user() {
            var actor = currentActor.required();
            return ResponseEntity.ok(Map.of("userId", actor.userId(), "admin", actor.hasRole("ADMIN")));
        }

        @GetMapping("/api/admin/_auth-fixture")
        ResponseEntity<Map<String, Object>> admin() {
            var actor = currentActor.required();
            return ResponseEntity.ok(Map.of("userId", actor.userId(), "admin", actor.hasRole("ADMIN")));
        }
    }
}
