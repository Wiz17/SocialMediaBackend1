package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String username;

    @NotBlank(message = "Bio is required")
    @Size(min = 2, max = 100, message = "Bio must be between 2 and 100 characters")
    private String bio;

    private String profilePhotoUrl;

}
