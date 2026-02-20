package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.PostsDTO;
import com.example.demo.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping(value = "/create-post", consumes = "multipart/form-data")
    public ResponseEntity<PostsDTO> createPost(
            @RequestParam("image") MultipartFile image,
            @RequestParam("description") String description) {

        return new ResponseEntity<>(postService.createPost(image, description), HttpStatus.CREATED);
    }
}
