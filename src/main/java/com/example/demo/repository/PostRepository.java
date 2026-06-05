package com.example.demo.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Posts;
import com.example.demo.entity.User;

@Repository
public interface PostRepository extends JpaRepository<Posts, UUID> {
    Page<Posts> findByUser(User user, Pageable pageable);
}
