package dev.usermanagement.auth;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuthCredentialRepository extends JpaRepository<AuthCredentialEntity, Long> {}
