package com.example.demo.dto;

import com.example.demo.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private Boolean profileComplete;
    private LocalDateTime createdAt;
}
