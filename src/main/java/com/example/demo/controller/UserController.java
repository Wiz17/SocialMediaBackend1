package com.example.demo.controller;

import com.example.demo.dto.CreateProfileRequest;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.service.ProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService ProfileService;
    
    // Create here /account route for account creation.
    @PostMapping("/create-profile")
    ResponseEntity<ProfileDTO> createAccount(@RequestBody CreateProfileRequest createProfileRequest){
        return new ResponseEntity<ProfileDTO>(ProfileService.createProfile(createProfileRequest),HttpStatus.CREATED);
    }
}
