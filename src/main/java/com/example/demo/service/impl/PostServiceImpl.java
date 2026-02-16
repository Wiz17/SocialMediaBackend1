package com.example.demo.service.impl;

import java.io.IOException;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PostsDTO;
import com.example.demo.dto.PostsRequest;
import com.example.demo.entity.User;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.PostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final ModelMapper modelMapper;

    @Override
    public PostsDTO createPost(PostsRequest postsRequest) {
        // TODO Auto-generated method stub
        User user = UserPrincipal.getCurrentUser();
        return null;
    }

    @Override
    public PostsDTO updatePost(PostsRequest postsRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePost'");
    }

}
