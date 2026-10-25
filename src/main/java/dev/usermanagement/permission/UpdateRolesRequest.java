package dev.usermanagement.permission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

enum RoleCode { USER, ADMIN }

record UpdateRolesRequest(@NotNull @NotEmpty List<@NotNull RoleCode> roleCodes) {}
