package com.example.demo.controller;

import com.example.demo.dto.ProfileRequest;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.service.ProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;
    
    // Create here /account route for account creation.
    @PostMapping(value = "/create-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDTO> createProfile(
            @RequestPart("profile") ProfileRequest profileRequest,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return new ResponseEntity<>(profileService.createProfile(profileRequest, photo), HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getProfile() {
        return new ResponseEntity<>(profileService.getProfile(), HttpStatus.OK);
    }

    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDTO> updateProfile(
            @RequestPart("profile") ProfileRequest profileRequest,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return new ResponseEntity<>(profileService.updateProfile(profileRequest, photo), HttpStatus.OK);
    }

}
