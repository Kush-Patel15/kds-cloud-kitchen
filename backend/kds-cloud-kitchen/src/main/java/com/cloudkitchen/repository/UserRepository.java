package com.cloudkitchen.repository;

import com.cloudkitchen.model.User;
import com.cloudkitchen.model.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    // List<User> findByIsActiveTrueOrderByFirstNameAscLastNameAsc();
    // List<User> findByRoleAndIsActiveTrueOrderByFirstNameAscLastNameAsc(Role role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}