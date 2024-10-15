package com.example.bookstore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNAUTHORIZED("You don't have permission", HttpStatus.FORBIDDEN),
    PASSWORD_NOT_MATCH("Password not match", HttpStatus.BAD_REQUEST),
    TOKEN_BLANK_EXCEPTION("Token must be not blank", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID_EXCEPTION("Token is invalid", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_ACTIVATED("Account not activated", HttpStatus.BAD_REQUEST),
    MAIL_TOKEN_INVALID("Mail token is invalid", HttpStatus.BAD_REQUEST),
    MAIL_TOKEN_EXPIRED("Activation token is expired. A new token has been sent to your emailActivation token is expired. A new token has been sent to your email",
            HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatusCode statusCode;


    ErrorCode(String message, HttpStatusCode statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
