package dev.usermanagement.user;

import java.time.OffsetDateTime;

/** OpenAPI `User` schema. Only these fields leave the server; UserEntity is never serialized directly. */
record UserResponse(long id, String loginId, String name, String email, String status, OffsetDateTime createdAt) {
    static UserResponse from(UserEntity u) {
        return new UserResponse(u.getId(), u.getLoginId(), u.getName(), u.getEmail(), u.getStatus(), u.getCreatedAt());
    }
}
