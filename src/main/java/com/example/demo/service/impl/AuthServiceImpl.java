package com.example.demo.service.impl;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Session;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.SessionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

   private final UserRepository userRepository;
   private final SessionRepository sessionRepository;
   private final ProfileRepository profileRepository;
   private final ModelMapper modelMapper;
   private final PasswordEncoder passwordEncoder;
   private final AuthenticationManager authenticationManager;
   private final JwtService jwtService;

   @Value("${jwt.refreshExpiration}")
   private long refreshExpiration;

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
   @Transactional
   public LoginResponse login(LoginRequest loginRequest) {
      try {
         Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
         );
         UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
         User user = userPrincipal.getUser();

         String accessToken = jwtService.generateToken(userPrincipal);
         String refreshToken = jwtService.generateRefreshToken(userPrincipal);

         Session session = Session.builder()
            .user(user)
            .refreshToken(refreshToken)
            .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
            .build();
         sessionRepository.save(session);

         // Check if user has a complete profile
         boolean profileComplete = profileRepository.findByUser(user)
            .map(profile -> profile.getIsComplete())
            .orElse(false);

         // Map user to DTO and add profile status
         UserDTO userDTO = modelMapper.map(user, UserDTO.class);
         userDTO.setProfileComplete(profileComplete);

         return new LoginResponse(accessToken, refreshToken, userDTO);
      } catch (BadCredentialsException e) {
         throw new InvalidCredentialsException("Invalid email or password");
      }
   }

   @Override
   public String refreshToken(String refreshToken) {
      Session session = sessionRepository.findByRefreshToken(refreshToken)
         .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

      if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
         sessionRepository.delete(session);
         throw new InvalidCredentialsException("Refresh token expired");
      }

      String email = jwtService.extractUsername(refreshToken);
      User user = userRepository.findByEmail(email)
         .orElseThrow(() -> new InvalidCredentialsException("User not found"));

      UserPrincipal userPrincipal = new UserPrincipal(user);
      return jwtService.generateToken(userPrincipal);
   }

   @Override
   @Transactional
   public void logout(String refreshToken) {
      sessionRepository.deleteByRefreshToken(refreshToken);
   }
}
