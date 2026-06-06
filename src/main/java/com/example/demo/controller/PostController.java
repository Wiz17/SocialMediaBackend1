package com.example.demo.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PatchMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<PostsDTO> updatePost(
            @PathVariable UUID postId,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String description) {

        return ResponseEntity.ok(postService.updatePost(postId, image, description));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Page<PostsDTO>> getUserPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(postService.getUserPosts(pageable));
    }
}
