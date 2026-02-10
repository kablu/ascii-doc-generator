package com.example.identityverification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailVerificationRequest {

    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotBlank(message = "Corporate email is required")
    @Email(message = "Must be a valid email address")
    private String corporateEmail;

    public EmailVerificationRequest() {}

    public EmailVerificationRequest(String requestId, String corporateEmail) {
        this.requestId = requestId;
        this.corporateEmail = corporateEmail;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCorporateEmail() { return corporateEmail; }
    public void setCorporateEmail(String corporateEmail) { this.corporateEmail = corporateEmail; }
}
