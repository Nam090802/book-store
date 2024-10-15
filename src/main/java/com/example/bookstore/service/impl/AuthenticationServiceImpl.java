package com.example.bookstore.service.impl;

import com.example.bookstore.dto.request.RegistrationRequest;
import com.example.bookstore.dto.request.SignInRequest;
import com.example.bookstore.dto.response.TokenResponse;
import com.example.bookstore.exception.AppException;
import com.example.bookstore.exception.ErrorCode;
import com.example.bookstore.model.BlackListToken;
import com.example.bookstore.model.MailToken;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.BlackListTokenRepository;
import com.example.bookstore.repository.MailTokenRepository;
import com.example.bookstore.repository.RoleRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.AuthenticationService;
import com.example.bookstore.service.EmailService;
import com.example.bookstore.util.enums.EmailTemplateName;
import com.example.bookstore.util.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MailTokenRepository mailTokenRepository;
    private final BlackListTokenRepository blackListTokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${mailing.front-end.activation-link}")
    private String activationUrl;

    @Override
    public TokenResponse login(SignInRequest signInRequest) {
        log.info("---------- Begin authenticate ----------");

        var user = userRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email is incorrect"));

        var isAccountActive = user.isActive();

        if (!isAccountActive) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVATED);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("---------- End authenticate ----------");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    @Override
    public TokenResponse refreshToken(HttpServletRequest request) {
        log.info("---------- Begin refreshToken ----------");
        try {
            String token = request.getHeader("x-token");

            if (StringUtils.isBlank(token)) {
                throw new AppException(ErrorCode.TOKEN_BLANK_EXCEPTION);
            }

            final String userEmail = jwtService.extractEmail(token, TokenType.REFRESH);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            if (!jwtService.isTokenValid(token, TokenType.REFRESH, user)) {
                throw new AppException(ErrorCode.TOKEN_INVALID_EXCEPTION);
            }

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("---------- End refreshToken ----------");

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .build();
        } catch (JwtException e) {
            throw new AppException(ErrorCode.TOKEN_INVALID_EXCEPTION);
        }
    }

    @Override
    public String logout(HttpServletRequest request) {
        String token = request.getHeader("x-token");

        if (StringUtils.isBlank(token)) {
            throw new AppException(ErrorCode.TOKEN_BLANK_EXCEPTION);
        }

        final String userEmail = jwtService.extractEmail(token, TokenType.ACCESS);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!jwtService.isTokenValid(token, TokenType.ACCESS, user)) {
            throw new AppException(ErrorCode.TOKEN_INVALID_EXCEPTION);
        }

        String tokenId = jwtService.extractClaim(token, TokenType.ACCESS, Claims::getId);
        Date expiryTime = jwtService.extractClaim(token, TokenType.ACCESS, Claims::getExpiration);

        BlackListToken blackListToken = BlackListToken.builder()
                .id(tokenId)
                .token(token)
                .expiryTime(expiryTime)
                .build();

        blackListTokenRepository.save(blackListToken);

        return "Logout successfully";
    }

    @Override
    public String register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isLocked(false)
                .isActive(false)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);

        sendValidationEmail(user);
        return "Register successfully";
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var token = generateAndSaveToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACCOUNT_ACTIVATION,
                activationUrl,
                token,
                "Activate your account");
    }

    private String generateAndSaveToken(User user) {
        String generatedToken = generateMailToken(6);
        var token = MailToken.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        mailTokenRepository.save(token);
        return generatedToken;
    }

    private String generateMailToken(int length) {
        String character = "0123456789";
        StringBuilder token = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            token.append(character.charAt(random.nextInt(character.length())));
        }
        return token.toString();
    }

    @Override
    public void activateAccount(String token) throws MessagingException {
        MailToken mailToken = mailTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.MAIL_TOKEN_INVALID));
        if (mailToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            sendValidationEmail(mailToken.getUser());
            throw new RuntimeException("Activation token is expired. A new token has been sent to your email");
        }
        var user = userRepository.findById(mailToken.getUser().getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        user.setActive(true);
        userRepository.save(user);
        mailToken.setValidatedAt(LocalDateTime.now());
        mailTokenRepository.save(mailToken);
    }
}
