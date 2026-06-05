package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.PostsDTO;

public interface PostService {
    PostsDTO createPost(MultipartFile image, String description);
    PostsDTO updatePost(MultipartFile image, String description);
    Page<PostsDTO> getUserPosts(Pageable pageable);
}
