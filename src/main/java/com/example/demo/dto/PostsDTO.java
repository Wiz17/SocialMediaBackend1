package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostsDTO {

    private UUID id;

    private String imageurl;

    private String description;

    private UUID userId;

    private String username;

    private String userProfileImage;

    private Integer likesCount;

    private Integer commentsCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
