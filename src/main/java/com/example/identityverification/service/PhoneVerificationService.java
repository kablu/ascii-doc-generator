package com.example.identityverification.service;

import com.example.identityverification.dto.PhoneCodeVerifyRequest;
import com.example.identityverification.dto.PhoneVerificationRequest;
import com.example.identityverification.dto.PhoneVerificationResponse;
import com.example.identityverification.exception.VerificationException;
import com.example.identityverification.exception.VerificationNotFoundException;
import com.example.identityverification.model.PhoneVerification;
import com.example.identityverification.model.VerificationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhoneVerificationService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_RESEND = 1;
    private static final int CODE_VALIDITY_MINUTES = 5;

    private final Map<String, PhoneVerification> verifications = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public PhoneVerificationResponse sendVerificationCode(PhoneVerificationRequest request) {
        PhoneVerification verification = new PhoneVerification();
        verification.setRequestId(request.getRequestId());
        verification.setMobileNumber(request.getMobileNumber());
        verification.setVerificationCode(generateSixDigitCode());
        verification.setStatus(VerificationStatus.SMS_SENT);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));

        verifications.put(verification.getId(), verification);

        return buildResponse(verification,
                "6-digit verification code sent to " + maskPhone(request.getMobileNumber()) +
                        ". Code valid for " + CODE_VALIDITY_MINUTES + " minutes.");
    }

    public PhoneVerificationResponse verifyCode(PhoneCodeVerifyRequest request) {
        PhoneVerification verification = verifications.get(request.getVerificationId());
        if (verification == null) {
            throw new VerificationNotFoundException("Verification not found for ID: " + request.getVerificationId());
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            verification.setStatus(VerificationStatus.EXPIRED);
            throw new VerificationException(
                    "Verification code has expired. Code is valid for " + CODE_VALIDITY_MINUTES + " minutes.",
                    "CODE_EXPIRED"
            );
        }

        if (verification.getAttemptCount() >= MAX_ATTEMPTS) {
            verification.setStatus(VerificationStatus.FAILED);
            throw new VerificationException(
                    "Maximum verification attempts exceeded. Only " + MAX_ATTEMPTS + " attempts allowed.",
                    "MAX_ATTEMPTS_EXCEEDED"
            );
        }

        verification.setAttemptCount(verification.getAttemptCount() + 1);

        if (!verification.getVerificationCode().equals(request.getCode())) {
            int remaining = MAX_ATTEMPTS - verification.getAttemptCount();
            if (remaining <= 0) {
                verification.setStatus(VerificationStatus.FAILED);
                throw new VerificationException(
                        "Invalid code. No attempts remaining. Verification failed.",
                        "VERIFICATION_FAILED"
                );
            }
            throw new VerificationException(
                    "Invalid verification code. " + remaining + " attempt(s) remaining.",
                    "INVALID_CODE"
            );
        }

        verification.setStatus(VerificationStatus.SMS_VERIFIED);
        verification.setVerifiedAt(LocalDateTime.now());

        return buildResponse(verification, "Phone number verified successfully.");
    }

    public PhoneVerificationResponse resendCode(String verificationId) {
        PhoneVerification verification = verifications.get(verificationId);
        if (verification == null) {
            throw new VerificationNotFoundException("Verification not found for ID: " + verificationId);
        }

        if (verification.getResendCount() >= MAX_RESEND) {
            throw new VerificationException(
                    "Maximum resend limit reached. Only " + MAX_RESEND + " resend allowed per request.",
                    "MAX_RESEND_EXCEEDED"
            );
        }

        verification.setResendCount(verification.getResendCount() + 1);
        verification.setVerificationCode(generateSixDigitCode());
        verification.setAttemptCount(0);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));
        verification.setStatus(VerificationStatus.SMS_SENT);

        return buildResponse(verification,
                "New verification code sent. Resend count: " + verification.getResendCount() + "/" + MAX_RESEND);
    }

    public PhoneVerificationResponse getStatus(String verificationId) {
        PhoneVerification verification = verifications.get(verificationId);
        if (verification == null) {
            throw new VerificationNotFoundException("Verification not found for ID: " + verificationId);
        }
        return buildResponse(verification, "Current verification status: " + verification.getStatus());
    }

    private String generateSixDigitCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private String maskPhone(String phone) {
        if (phone.length() <= 4) return "****";
        return "****" + phone.substring(phone.length() - 4);
    }

    private PhoneVerificationResponse buildResponse(PhoneVerification verification, String message) {
        PhoneVerificationResponse response = new PhoneVerificationResponse();
        response.setVerificationId(verification.getId());
        response.setRequestId(verification.getRequestId());
        response.setMobileNumber(maskPhone(verification.getMobileNumber()));
        response.setStatus(verification.getStatus());
        response.setMessage(message);
        response.setAttemptCount(verification.getAttemptCount());
        response.setMaxAttempts(MAX_ATTEMPTS);
        response.setResendCount(verification.getResendCount());
        response.setMaxResendAllowed(MAX_RESEND);
        response.setExpiresAt(verification.getExpiresAt());
        return response;
    }
}
