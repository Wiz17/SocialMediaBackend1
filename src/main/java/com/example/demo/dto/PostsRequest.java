package com.example.demo.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostsRequest {

    @NotNull(message = "Image is required")
    private MultipartFile image;

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 100, message = "Description must be between 2 and 100 characters")
    private String description;

}
