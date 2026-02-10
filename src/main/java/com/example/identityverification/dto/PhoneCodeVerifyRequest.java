package com.example.identityverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PhoneCodeVerifyRequest {

    @NotBlank(message = "Verification ID is required")
    private String verificationId;

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
    private String code;

    public PhoneCodeVerifyRequest() {}

    public PhoneCodeVerifyRequest(String verificationId, String code) {
        this.verificationId = verificationId;
        this.code = code;
    }

    public String getVerificationId() { return verificationId; }
    public void setVerificationId(String verificationId) { this.verificationId = verificationId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
