package com.example.demo.service;

import com.example.demo.dto.PostsDTO;

import org.springframework.web.multipart.MultipartFile;

public interface PostService {
    PostsDTO createPost(MultipartFile image, String description);
    PostsDTO updatePost(MultipartFile image, String description);
}
