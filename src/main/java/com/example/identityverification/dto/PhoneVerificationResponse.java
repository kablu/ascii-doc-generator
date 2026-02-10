package com.example.identityverification.dto;

import com.example.identityverification.model.VerificationStatus;

import java.time.LocalDateTime;

public class PhoneVerificationResponse {

    private String verificationId;
    private String requestId;
    private String mobileNumber;
    private VerificationStatus status;
    private String message;
    private int attemptCount;
    private int maxAttempts;
    private int resendCount;
    private int maxResendAllowed;
    private LocalDateTime expiresAt;

    public PhoneVerificationResponse() {}

    public String getVerificationId() { return verificationId; }
    public void setVerificationId(String verificationId) { this.verificationId = verificationId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public int getResendCount() { return resendCount; }
    public void setResendCount(int resendCount) { this.resendCount = resendCount; }
    public int getMaxResendAllowed() { return maxResendAllowed; }
    public void setMaxResendAllowed(int maxResendAllowed) { this.maxResendAllowed = maxResendAllowed; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
