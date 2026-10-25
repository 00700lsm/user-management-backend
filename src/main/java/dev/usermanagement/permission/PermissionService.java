package dev.usermanagement.permission;

import dev.usermanagement.shared.ApiException;
import dev.usermanagement.user.UserAccountLookup;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PermissionService {
    static final ApiException NOT_FOUND = new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.");
    static final ApiException INVALID = new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "요청 형식이 올바르지 않습니다.");

    private final RoleRepository roles;
    private final UserRoleRepository assignments;
    private final UserAccountLookup users;

    PermissionService(RoleRepository roles, UserRoleRepository assignments, UserAccountLookup users) {
        this.roles = roles;
        this.assignments = assignments;
        this.users = users;
    }

    List<RoleResponse> listRoles() {
        return roles.findAllByOrderByCodeAsc().stream()
            .map(role -> new RoleResponse(role.getCode(), role.getName()))
            .toList();
    }

    UserRolesResponse getUserRoles(long userId) {
        requireUser(userId);
        return new UserRolesResponse(userId, assigned(userId));
    }

    /** Replaces the user's whole role set. USER and ADMIN may be held together. */
    @Transactional
    UserRolesResponse replaceUserRoles(long userId, List<RoleCode> requested) {
        requireUser(userId);
        if (requested.stream().distinct().count() != requested.size()) throw INVALID;
        assignments.deleteAll(assignments.findByUserIdOrderByRoleCodeAsc(userId));
        assignments.flush();
        for (RoleCode code : requested) assignments.save(new UserRoleEntity(userId, code.name()));
        return new UserRolesResponse(userId, assigned(userId));
    }

    private void requireUser(long userId) {
        if (users.findStatus(userId).isEmpty()) throw NOT_FOUND;
    }

    private List<String> assigned(long userId) {
        return assignments.findByUserIdOrderByRoleCodeAsc(userId).stream().map(UserRoleEntity::getRoleCode).toList();
    }
}
