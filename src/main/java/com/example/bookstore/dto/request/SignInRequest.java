package com.example.bookstore.dto.request;

import com.example.bookstore.util.enums.Platform;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest implements Serializable {
    //@NotBlank(message = "email must be not null")
    private String email;

    //@NotBlank(message = "password must be not null")
    private String password;

    //@NotNull
    private Platform platform;

    private String deviceToken;

}
