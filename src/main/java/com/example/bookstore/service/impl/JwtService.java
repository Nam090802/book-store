package com.example.bookstore.service.impl;

import com.example.bookstore.model.User;
import com.example.bookstore.util.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.example.bookstore.util.enums.TokenType.ACCESS;
import static com.example.bookstore.util.enums.TokenType.REFRESH;

@Service
public class JwtService {

    @Value("${jwt.accessTokenKey}")
    private String accessTokenKey;

    @Value("${jwt.refreshTokenKey}")
    private String refreshTokenKey;

    @Value("${jwt.accessTokenExpiryTime}")
    private long accessTokenExpiryTime;

    @Value("${jwt.refreshTokenExpiryTime}")
    private long refreshTokenExpiryTime;

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims)
                .setId(String.valueOf(UUID.randomUUID()))
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * accessTokenExpiryTime))
                .signWith(getKey(ACCESS), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * refreshTokenExpiryTime))
                .signWith(getKey(REFRESH), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(TokenType tokenType) {
        byte[] keyBytes;
        if (tokenType.equals(ACCESS)){
            keyBytes = Decoders.BASE64.decode(accessTokenKey);
        } else {
            keyBytes = Decoders.BASE64.decode(refreshTokenKey);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey(tokenType))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, TokenType tokenType,Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token, TokenType tokenType) {
        if (tokenType.equals(ACCESS)){
            return extractClaim(token, ACCESS, Claims::getSubject);
        } else {
            return extractClaim(token, REFRESH, Claims::getSubject);
        }
    }

    public Date extractExpiration(String token, TokenType tokenType) {
        if (tokenType.equals(ACCESS)){
            return extractClaim(token, ACCESS, Claims::getExpiration);
        } else {
            return extractClaim(token, REFRESH, Claims::getExpiration);
        }
    }

    public boolean isTokenExpired(String token, TokenType tokenType) {
        if (tokenType.equals(ACCESS)){
            return !extractExpiration(token, ACCESS).before(new Date());
        } else {
            return !extractExpiration(token, REFRESH).before(new Date());
        }
    }

    public boolean isTokenValid (String token, TokenType tokenType, UserDetails userDetails) {
        if (tokenType.equals(ACCESS)){
            return extractEmail(token, ACCESS).equals(userDetails.getUsername()) && isTokenExpired(token, ACCESS);
        } else {
            return extractEmail(token, REFRESH).equals(userDetails.getUsername()) && isTokenExpired(token, REFRESH);
        }
    }
}
