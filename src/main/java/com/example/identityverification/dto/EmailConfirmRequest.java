package com.example.identityverification.dto;

import jakarta.validation.constraints.NotBlank;

public class EmailConfirmRequest {

    @NotBlank(message = "Verification token is required")
    private String token;

    public EmailConfirmRequest() {}

    public EmailConfirmRequest(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
