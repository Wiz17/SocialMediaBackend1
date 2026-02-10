package com.example.demo.service;

import com.example.demo.dto.CreateProfileRequest;
import com.example.demo.dto.ProfileDTO;

public interface ProfileService {
    ProfileDTO createProfile(CreateProfileRequest createProfileRequest);
}
