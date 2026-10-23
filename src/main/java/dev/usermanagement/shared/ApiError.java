package dev.usermanagement.shared;

/** Error body shared by every API (OpenAPI ApiError). */
public record ApiError(String code, String message) {}
