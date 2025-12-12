package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.Role;
import com.autocare.autocarebackend.models.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(ERole name);
}
