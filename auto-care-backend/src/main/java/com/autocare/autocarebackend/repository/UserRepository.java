package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.EAccountStatus;
import com.autocare.autocarebackend.models.Role;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Override
    Optional<User> findById(Long aLong);

    boolean existsByUsername(String username);

    boolean existsByNic(String nic);

    @Override
    List<User> findAll();

    long count();

    List<User> findAllByRolesContaining(Role role);

    // Add new methods for approval system
    List<User> findAllByAccountStatus(EAccountStatus status);

    List<User> findAllByAccountStatusAndRolesContaining(EAccountStatus status, Role role);
}