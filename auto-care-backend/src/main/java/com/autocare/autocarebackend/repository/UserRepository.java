package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.ERole;
import com.autocare.autocarebackend.models.Role;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Override
    Optional<User> findById(Long aLong);
    boolean existsByUsername(String username);
    boolean existsByNic(String nic);
    @Override
    List<User> findAll();

//    @Query(value = "Select u FROM Role u where u.id = 4 ")
//    List<User> getAllLCompany();

//    @Query(value = "SELECT u FROM User u where u.roles = :roles")
//    List<User> FilterByRole(@Param("roles") String[] roles);

    List<User> findAllByRolesContaining(Role role);

}
