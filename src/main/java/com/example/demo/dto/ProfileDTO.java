package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    private UUID id;
    private String username;
    private String bio;
    private String profilePhotoUrl;
    private LocalDateTime createdAt;

}
