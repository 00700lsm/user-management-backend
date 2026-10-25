package dev.usermanagement.permission;

import jakarta.persistence.*;

@Entity @Table(name = "app_role")
public class RoleEntity {
    @Id @Column(name = "code", length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    protected RoleEntity() {}

    String getCode() { return code; }
    String getName() { return name; }
}
