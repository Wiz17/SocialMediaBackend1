package com.example.demo.service;

import com.example.demo.dto.PostsDTO;
import com.example.demo.dto.PostsRequest;

public interface PostService {
    PostsDTO createPost(PostsRequest postRequest);
    PostsDTO updatePost(PostsRequest postsRequest);
}
