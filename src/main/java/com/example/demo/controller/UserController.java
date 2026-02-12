package com.example.demo.controller;

import com.example.demo.dto.ProfileRequest;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.service.ProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;
    
    // Create here /account route for account creation.
    @PostMapping("/create-profile")
    public ResponseEntity<ProfileDTO> createProfile(@RequestBody ProfileRequest profileRequest) {
        return new ResponseEntity<>(profileService.createProfile(profileRequest), HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getProfile() {
        return new ResponseEntity<>(profileService.getProfile(), HttpStatus.OK);
    }

}
