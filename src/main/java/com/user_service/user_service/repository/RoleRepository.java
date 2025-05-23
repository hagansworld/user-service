package com.user_service.user_service.repository;

import com.user_service.user_service.entity.Role;
import com.user_service.user_service.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
//    Role findByRole(UserRole role);

    Optional<Role> findByRole(UserRole role);

    List<Role> findByRoleIn(List<String> role);
}
