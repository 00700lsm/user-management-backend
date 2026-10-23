package dev.usermanagement.user;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcUserAccountLookup implements UserAccountLookup {
    private final JdbcTemplate jdbc;

    JdbcUserAccountLookup(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Optional<UserAccount> findByLoginId(String loginId) {
        return jdbc.query("select id, login_id, status from app_user where login_id = ?",
            (rs, i) -> new UserAccount(rs.getLong(1), rs.getString(2), rs.getString(3)), loginId)
            .stream().findFirst();
    }

    @Override
    public Optional<String> findStatus(long userId) {
        return jdbc.queryForList("select status from app_user where id = ?", String.class, userId)
            .stream().findFirst();
    }
}
