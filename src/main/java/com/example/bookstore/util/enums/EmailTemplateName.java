package com.example.bookstore.util.enums;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACCOUNT_ACTIVATION("activate_account"),
    ;

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
