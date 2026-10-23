package dev.usermanagement.permission;

import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcRoleLookup implements RoleLookup {
    private final JdbcTemplate jdbc;

    JdbcRoleLookup(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Set<String> currentRoles(long userId) {
        return Set.copyOf(jdbc.queryForList("select role_code from user_role where user_id = ?", String.class, userId));
    }
}
