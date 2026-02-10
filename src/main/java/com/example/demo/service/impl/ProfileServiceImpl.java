package com.example.demo.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CreateProfileRequest;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.entity.Profile;
import com.example.demo.entity.User;
import com.example.demo.exception.ProfileAlreadyExistsException;
import com.example.demo.exception.UsernameAlreadyExistsException;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ProfileService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {


    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProfileDTO createProfile(CreateProfileRequest createProfileRequest) {
        User user = UserPrincipal.getCurrentUser();

        // Check if user already has a profile
        if (profileRepository.existsByUser(user)) {
            throw new ProfileAlreadyExistsException("Profile already exists for this user");
        }

        if(profileRepository.existsByUsername(createProfileRequest.getUsername())){
            throw new UsernameAlreadyExistsException("Username already exist");
        }

        // Map request to profile entity
        Profile profile = modelMapper.map(createProfileRequest, Profile.class);
        profile.setUser(user);           // Link profile to user
        profile.setIsComplete(true);     // Mark profile as complete

        Profile savedProfile = profileRepository.save(profile);

        return modelMapper.map(savedProfile, ProfileDTO.class);
    }

}
