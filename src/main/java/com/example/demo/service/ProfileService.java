package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ProfileRequest;
import com.example.demo.dto.ProfileDTO;

public interface ProfileService {
    ProfileDTO createProfile(ProfileRequest profileRequest, MultipartFile profilePhoto);
    ProfileDTO getProfile();
    ProfileDTO updateProfile(ProfileRequest profileRequest, MultipartFile profilePhoto);
}
