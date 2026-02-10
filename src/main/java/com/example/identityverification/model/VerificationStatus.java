package com.example.identityverification.model;

public enum VerificationStatus {
    PENDING,
    EMAIL_SENT,
    EMAIL_VERIFIED,
    SMS_SENT,
    SMS_VERIFIED,
    VERIFIED,
    FAILED,
    EXPIRED
}
