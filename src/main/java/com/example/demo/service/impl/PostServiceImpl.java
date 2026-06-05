package com.example.demo.service.impl;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.PostsDTO;
import com.example.demo.entity.Posts;
import com.example.demo.entity.Profile;
import com.example.demo.entity.User;
import com.example.demo.exception.FileUploadException;
import com.example.demo.helper.SupabaseStorageService;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.PostService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final SupabaseStorageService supabaseStorageService;

    @Override
    @Transactional
    public PostsDTO createPost(MultipartFile image, String description) {
        User user = UserPrincipal.getCurrentUser();

        String postImageUrl;
        try {
            postImageUrl = supabaseStorageService.uploadImage(image, "posts");
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload post image: " + e.getMessage());
        }

        Posts posts = Posts.builder()
                .description(description)
                .imageurl(postImageUrl)
                .user(user)
                .likesCount(0)
                .commentsCount(0)
                .build();

        Posts savedPost = postRepository.save(posts);

        Profile profile = profileRepository.findByUser(user).orElse(null);

        return PostsDTO.builder()
                .id(savedPost.getId())
                .imageurl(savedPost.getImageurl())
                .description(savedPost.getDescription())
                .userId(user.getId())
                .username(profile != null ? profile.getUsername() : null)
                .userProfileImage(profile != null ? profile.getProfilePhotoUrl() : null)
                .likesCount(savedPost.getLikesCount())
                .commentsCount(savedPost.getCommentsCount())
                .createdAt(savedPost.getCreatedAt())
                .updatedAt(savedPost.getUpdatedAt())
                .build();
    }

    @Override
    public Page<PostsDTO> getUserPosts(Pageable pageable) {
        User user = UserPrincipal.getCurrentUser();

        return postRepository.findByUser(user, pageable)
                .map(post -> PostsDTO.builder()
                        .id(post.getId())
                        .imageurl(post.getImageurl())
                        .description(post.getDescription())
                        .userId(user.getId())
                        .likesCount(post.getLikesCount())
                        .commentsCount(post.getCommentsCount())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .build());
    }

    @Override
    public PostsDTO updatePost(MultipartFile image, String description) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePost'");
    }

}
