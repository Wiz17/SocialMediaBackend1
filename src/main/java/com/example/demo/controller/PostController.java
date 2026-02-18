package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PostsDTO;
import com.example.demo.dto.PostsRequest;
import com.example.demo.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/create-post")
    public ResponseEntity<PostsDTO> createPost(@RequestBody PostsRequest postsRequest){
        return new ResponseEntity<PostsDTO>(postService.createPost(postsRequest),HttpStatus.OK);
    }
}
