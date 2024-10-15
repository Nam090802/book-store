package com.example.bookstore.service;

import com.example.bookstore.dto.request.RegistrationRequest;
import com.example.bookstore.dto.request.SignInRequest;
import com.example.bookstore.dto.response.TokenResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    TokenResponse login(SignInRequest signInRequest);
    TokenResponse refreshToken(HttpServletRequest request);
    String logout(HttpServletRequest request);
    String register(RegistrationRequest request) throws MessagingException;
    void activateAccount(String token) throws MessagingException;

}
