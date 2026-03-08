// RoleRepository.java
package com.smartstore.domain.user.repository;

import com.smartstore.common.enums.RoleName;
import com.smartstore.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}