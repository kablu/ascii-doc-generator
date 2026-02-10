package com.example.identityverification.dto;

import com.example.identityverification.model.VerificationStatus;

import java.time.LocalDateTime;

public class EmailVerificationResponse {

    private String verificationId;
    private String requestId;
    private String corporateEmail;
    private VerificationStatus status;
    private String message;
    private int resendCount;
    private int maxResendAllowed;
    private LocalDateTime expiresAt;

    public EmailVerificationResponse() {}

    public String getVerificationId() { return verificationId; }
    public void setVerificationId(String verificationId) { this.verificationId = verificationId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCorporateEmail() { return corporateEmail; }
    public void setCorporateEmail(String corporateEmail) { this.corporateEmail = corporateEmail; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getResendCount() { return resendCount; }
    public void setResendCount(int resendCount) { this.resendCount = resendCount; }
    public int getMaxResendAllowed() { return maxResendAllowed; }
    public void setMaxResendAllowed(int maxResendAllowed) { this.maxResendAllowed = maxResendAllowed; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
