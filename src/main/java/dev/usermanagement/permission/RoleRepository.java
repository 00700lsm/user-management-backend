package dev.usermanagement.permission;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface RoleRepository extends JpaRepository<RoleEntity, String> {
    List<RoleEntity> findAllByOrderByCodeAsc();
}
