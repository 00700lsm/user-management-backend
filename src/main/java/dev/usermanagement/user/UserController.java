package dev.usermanagement.user;

import dev.usermanagement.shared.ApiException;
import dev.usermanagement.shared.CurrentActor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
class UserController {
    private final UserRepository users;
    private final CurrentActor currentActor;

    UserController(UserRepository users, CurrentActor currentActor) {
        this.users = users;
        this.currentActor = currentActor;
    }

    /** REQ-USER-002. Whose profile is decided only by the verified token, never by request input. */
    @GetMapping("/me")
    UserResponse me() {
        long userId = currentActor.required().userId();
        // The Auth filter has already checked this user is ACTIVE; a missing row means the identity is gone.
        return users.findById(userId).map(UserResponse::from)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
    }
}
