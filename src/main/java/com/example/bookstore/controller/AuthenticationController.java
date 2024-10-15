package com.example.bookstore.controller;

import com.example.bookstore.dto.request.RegistrationRequest;
import com.example.bookstore.dto.request.SignInRequest;
import com.example.bookstore.dto.response.ApiResponse;
import com.example.bookstore.dto.response.TokenResponse;
import com.example.bookstore.service.AuthenticationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Tag(name = "Authentication Controller")
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody SignInRequest signInRequest) {
        var result = authenticationService.login(signInRequest);
        return ApiResponse.<TokenResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return new ResponseEntity<>(authenticationService.logout(request), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(HttpServletRequest request) {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<TokenResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequest request) throws MessagingException {
        return new ResponseEntity<>(authenticationService.register(request), HttpStatus.CREATED);
    }

    @GetMapping("/activate-account")
    public ResponseEntity<String> activateAccount(@RequestParam String token) throws MessagingException {
        authenticationService.activateAccount(token);
        return new ResponseEntity<>("Account activated successfully", HttpStatus.OK);
    }
}
