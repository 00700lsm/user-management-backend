package dev.usermanagement.permission;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {
    List<UserRoleEntity> findByUserIdOrderByRoleCodeAsc(long userId);
}
