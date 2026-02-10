package com.example.demo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Session;
import com.example.demo.entity.User;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshToken(String refreshToken);

    void deleteByUser(User user);

    void deleteByRefreshToken(String refreshToken);

}
