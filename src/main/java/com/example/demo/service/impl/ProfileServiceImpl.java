package com.example.demo.service.impl;

import java.io.IOException;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ProfileRequest;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.entity.Profile;
import com.example.demo.entity.User;
import com.example.demo.exception.FileUploadException;
import com.example.demo.exception.ProfileAlreadyExistsException;
import com.example.demo.exception.ProfileNotFoundException;
import com.example.demo.exception.UsernameAlreadyExistsException;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ProfileService;
import com.example.demo.service.SupabaseStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {


    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;
    private final SupabaseStorageService supabaseStorageService;

    @Override
    @Transactional
    public ProfileDTO createProfile(ProfileRequest profileRequest, MultipartFile profilePhoto) {
        User user = UserPrincipal.getCurrentUser();

        // Check if user already has a profile
        if (profileRepository.existsByUser(user)) {
            throw new ProfileAlreadyExistsException("Profile already exists for this user");
        }

        if(profileRepository.existsByUsername(profileRequest.getUsername())){
            throw new UsernameAlreadyExistsException("Username already exist");
        }

        // Map request to profile entity
        Profile profile = modelMapper.map(profileRequest, Profile.class);
        profile.setUser(user);           // Link profile to user
        profile.setIsComplete(true);     // Mark profile as complete

        // Upload profile photo if provided
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                String photoUrl = supabaseStorageService.uploadProfilePhoto(profilePhoto);
                profile.setProfilePhotoUrl(photoUrl);
            } catch (IOException e) {
                throw new FileUploadException("Failed to upload profile photo: " + e.getMessage(), e);
            }
        }

        Profile savedProfile = profileRepository.save(profile);

        return modelMapper.map(savedProfile, ProfileDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getProfile() {
        // Get current authenticated user from JWT token via SecurityContext
        User user = UserPrincipal.getCurrentUser();

        // Find profile by user
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for this user"));

        // Map to DTO and return
        return modelMapper.map(profile, ProfileDTO.class);
    }

    @Override
    @Transactional
    public ProfileDTO updateProfile(ProfileRequest profileRequest, MultipartFile profilePhoto) {
        User user = UserPrincipal.getCurrentUser();

        // Fetch existing profile
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for this user"));

        // Check if new username is taken by another user
        if (!profile.getUsername().equals(profileRequest.getUsername())
            && profileRepository.existsByUsername(profileRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exist");
        }

        // Update profile fields
        profile.setUsername(profileRequest.getUsername());
        profile.setBio(profileRequest.getBio());

        // Upload new profile photo if provided
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                // Delete old photo if exists
                if (profile.getProfilePhotoUrl() != null && !profile.getProfilePhotoUrl().isEmpty()) {
                    supabaseStorageService.deleteFile(profile.getProfilePhotoUrl());
                }

                // Upload new photo
                String photoUrl = supabaseStorageService.uploadProfilePhoto(profilePhoto);
                profile.setProfilePhotoUrl(photoUrl);
            } catch (IOException e) {
                throw new FileUploadException("Failed to upload profile photo: " + e.getMessage(), e);
            }
        }

        Profile savedProfile = profileRepository.save(profile);

        return modelMapper.map(savedProfile, ProfileDTO.class);
    }
    

}
