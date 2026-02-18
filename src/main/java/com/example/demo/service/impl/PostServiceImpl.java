package com.example.demo.service.impl;

import java.io.IOException;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PostsDTO;
import com.example.demo.dto.PostsRequest;
import com.example.demo.entity.Posts;
import com.example.demo.entity.User;
import com.example.demo.exception.FileUploadException;
// import com.example.demo.helper.SupabaseStorageService;
import com.example.demo.repository.PostRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.PostService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    // private final SupabaseStorageService supabaseStorageService;

    @Override
    @Transactional
    public PostsDTO createPost(PostsRequest postsRequest) {
        // TODO Auto-generated method stub
        User user = UserPrincipal.getCurrentUser();

        Posts posts = modelMapper.map(postsRequest , Posts.class);
        posts.setUser(user);
        // try{
            // String postImageUrl = supabaseStorageService.uploadProfilePhoto(postsRequest.getImage());
            String postImageUrl = "URL_COMES_HERE";
            posts.setImageurl(postImageUrl);

        // }catch (IOException e) {
            // throw new FileUploadException("Failed to upload posts image: " + e.getMessage(), e);
        // }

        Posts savedPost = postRepository.save(posts);

        return modelMapper.map(savedPost , PostsDTO.class);
    }

    @Override
    public PostsDTO updatePost(PostsRequest postsRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePost'");
    }

}
