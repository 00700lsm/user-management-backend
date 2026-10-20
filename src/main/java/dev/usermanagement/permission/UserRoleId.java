package dev.usermanagement.permission;

import java.io.Serializable;
import java.util.Objects;

public class UserRoleId implements Serializable {
    public Long userId;
    public String roleCode;

    public UserRoleId() {}
    public UserRoleId(Long userId, String roleCode) {
        this.userId = userId;
        this.roleCode = roleCode;
    }
    @Override public boolean equals(Object o) {
        if (!(o instanceof UserRoleId other)) return false;
        return Objects.equals(userId, other.userId) && Objects.equals(roleCode, other.roleCode);
    }
    @Override public int hashCode() { return Objects.hash(userId, roleCode); }
}
