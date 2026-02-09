package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public UserDTO signup(@RequestBody SignupRequest signupRequest) {
        return authService.signup(signupRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponse(response.getAccessToken(), null, response.getUser()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        String newAccessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok()
                .body(new LoginResponse(newAccessToken, null, null));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue("refreshToken") String refreshToken) {
        authService.logout(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Logged out");
    }
}
