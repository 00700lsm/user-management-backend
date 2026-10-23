package dev.usermanagement.shared;

/** No DB-verified actor in the SecurityContext; mapped to 401 UNAUTHORIZED. */
public class ActorUnavailableException extends IllegalStateException {
    public ActorUnavailableException(String message) { super(message); }
}
