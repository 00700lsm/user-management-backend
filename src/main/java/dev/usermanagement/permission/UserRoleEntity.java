package dev.usermanagement.permission;

import jakarta.persistence.*;

@Entity @Table(name = "user_role")
@IdClass(UserRoleId.class)
public class UserRoleEntity {
    @Id @Column(name = "user_id")
    private Long userId;

    @Id @Column(name = "role_code", length = 20)
    private String roleCode;

    protected UserRoleEntity() {}

    UserRoleEntity(long userId, String roleCode) {
        this.userId = userId;
        this.roleCode = roleCode;
    }

    String getRoleCode() { return roleCode; }
}
