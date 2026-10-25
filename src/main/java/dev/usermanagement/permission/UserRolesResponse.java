package dev.usermanagement.permission;

import java.util.List;

record UserRolesResponse(long userId, List<String> roleCodes) {}
