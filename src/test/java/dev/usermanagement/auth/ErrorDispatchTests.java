package dev.usermanagement.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Real HTTP so the servlet ERROR dispatch (/error) happens; MockMvc does not forward. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ErrorDispatchTests {
    static final String PW = "correct-horse-battery";

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired CredentialRegistrar registrar;

    final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void seed() {
        jdbc.update("delete from auth_credential");
        jdbc.update("delete from user_role");
        jdbc.update("delete from app_user");
        jdbc.update("insert into app_user(login_id, email, name, status) values ('alice','alice@example.com','alice','ACTIVE')");
        long id = jdbc.queryForObject("select id from app_user where login_id = 'alice'", Long.class);
        jdbc.update("insert into user_role(user_id, role_code) values (?, 'USER')", id);
        registrar.createCredential(id, PW);
    }

    HttpResponse<String> send(HttpRequest.Builder b) throws Exception {
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void unmappedRouteWithValidTokenIs404NotMaskedAs401() throws Exception {
        var login = send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"loginId\":\"alice\",\"password\":\"" + PW + "\"}")));
        assertEquals(200, login.statusCode());
        String token = JsonPath.read(login.body(), "$.accessToken");

        var res = send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/does-not-exist"))
            .header("Authorization", "Bearer " + token).GET());
        assertEquals(404, res.statusCode());
        assertEquals("NOT_FOUND", JsonPath.read(res.body(), "$.code"));
    }

    @Test
    void unmappedRouteWithoutTokenStill401() throws Exception {
        var res = send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/does-not-exist")).GET());
        assertEquals(401, res.statusCode());
        assertTrue(res.body().contains("UNAUTHORIZED"));
    }
}
