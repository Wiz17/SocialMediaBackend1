package com.example.demo.service;

import com.example.demo.dto.ProfileRequest;
import com.example.demo.dto.ProfileDTO;

public interface ProfileService {
    ProfileDTO createProfile(ProfileRequest profileRequest);
    ProfileDTO getProfile();
    ProfileDTO updateProfile(ProfileRequest profileRequest);
}
