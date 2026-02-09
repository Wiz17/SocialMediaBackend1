package com.example.demo.repository;

import com.example.demo.entity.Profile;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUser(User user);
    boolean existsByUser(User user);
    Optional<Profile> findByUsername(String username);
}
