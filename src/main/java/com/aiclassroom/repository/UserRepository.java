package com.aiclassroom.repository;

import com.aiclassroom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
}
