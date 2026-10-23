package dev.usermanagement.user;

import org.springframework.data.jpa.repository.JpaRepository;

/** USER (app_user) is owned by User (B). Package-private: other modules use UserAccountLookup. */
interface UserRepository extends JpaRepository<UserEntity, Long> {}
