package dev.usermanagement.permission;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
class PermissionController {
    private final PermissionService permissions;

    PermissionController(PermissionService permissions) { this.permissions = permissions; }

    /** REQ-PERM-001. */
    @GetMapping("/roles")
    List<RoleResponse> listRoles() { return permissions.listRoles(); }

    /** REQ-PERM-002. */
    @GetMapping("/users/{id}/roles")
    UserRolesResponse getUserRoles(@PathVariable long id) { return permissions.getUserRoles(id); }

    /** REQ-PERM-002. The body replaces the user's current role set. */
    @PutMapping("/users/{id}/roles")
    UserRolesResponse replaceUserRoles(@PathVariable long id, @Valid @RequestBody UpdateRolesRequest request) {
        return permissions.replaceUserRoles(id, request.roleCodes());
    }
}
