package com.example.identityverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PhoneVerificationRequest {

    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Must be a valid mobile number in E.164 format")
    private String mobileNumber;

    public PhoneVerificationRequest() {}

    public PhoneVerificationRequest(String requestId, String mobileNumber) {
        this.requestId = requestId;
        this.mobileNumber = mobileNumber;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
}
