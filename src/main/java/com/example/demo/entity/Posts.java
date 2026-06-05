package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.UUID;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_user_id", columnList = "user_id"),
    @Index(name = "idx_posts_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    private String imageurl;

    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer likesCount;

    private Integer commentsCount;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
