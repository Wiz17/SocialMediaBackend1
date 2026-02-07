package com.example.demo.service.impl;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

   private final UserRepository userRepository;
   private final ModelMapper modelMapper;
   private final PasswordEncoder passwordEncoder;
   private final AuthenticationManager authenticationManager;
   private final JwtService jwtService;


   @Override
   @Transactional
   public UserDTO signup(SignupRequest signupRequest) {
      if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
         throw new EmailAlreadyExistsException("Email already exists");
      }
      User user = modelMapper.map(signupRequest, User.class);
      user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
      user.setRole(Role.USER);
      User savedUser = userRepository.save(user);
      return modelMapper.map(savedUser, UserDTO.class);
   }

   @Override
   public String login(LoginRequest loginRequest) {
      try {
         Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
         );
         UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
         return jwtService.generateToken(userPrincipal);
      } catch (BadCredentialsException e) {
         throw new InvalidCredentialsException("Invalid email or password");
      }
   }

   @Override
   public String refreshToken(String refreshToken) {
      return null;
   }
}
