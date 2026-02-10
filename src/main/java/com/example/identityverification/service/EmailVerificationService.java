package com.example.identityverification.service;

import com.example.identityverification.dto.EmailConfirmRequest;
import com.example.identityverification.dto.EmailVerificationRequest;
import com.example.identityverification.dto.EmailVerificationResponse;
import com.example.identityverification.exception.VerificationException;
import com.example.identityverification.exception.VerificationNotFoundException;
import com.example.identityverification.model.EmailVerification;
import com.example.identityverification.model.VerificationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    private static final int MAX_RESEND_COUNT = 3;

    private final Map<String, EmailVerification> verifications = new ConcurrentHashMap<>();
    private final Map<String, String> tokenToVerificationId = new ConcurrentHashMap<>();

    public EmailVerificationResponse sendVerificationEmail(EmailVerificationRequest request) {
        EmailVerification verification = new EmailVerification();
        verification.setRequestId(request.getRequestId());
        verification.setCorporateEmail(request.getCorporateEmail());

        String token = UUID.randomUUID().toString();
        verification.setVerificationLink("/api/v1/email-verification/confirm?token=" + token);
        verification.setStatus(VerificationStatus.EMAIL_SENT);

        verifications.put(verification.getId(), verification);
        tokenToVerificationId.put(token, verification.getId());

        return buildResponse(verification, "Verification email sent to " + request.getCorporateEmail());
    }

    public EmailVerificationResponse confirmEmail(EmailConfirmRequest request) {
        String verificationId = tokenToVerificationId.get(request.getToken());
        if (verificationId == null) {
            throw new VerificationNotFoundException("Invalid verification token");
        }

        EmailVerification verification = verifications.get(verificationId);
        if (verification == null) {
            throw new VerificationNotFoundException("Verification not found");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            verification.setStatus(VerificationStatus.EXPIRED);
            throw new VerificationException("Verification link has expired. Link is valid for 24 hours.", "LINK_EXPIRED");
        }

        verification.setStatus(VerificationStatus.EMAIL_VERIFIED);
        verification.setVerifiedAt(LocalDateTime.now());

        return buildResponse(verification, "Email verified successfully. Request marked as verified.");
    }

    public EmailVerificationResponse resendVerificationEmail(String verificationId) {
        EmailVerification verification = verifications.get(verificationId);
        if (verification == null) {
            throw new VerificationNotFoundException("Verification not found for ID: " + verificationId);
        }

        if (verification.getResendCount() >= MAX_RESEND_COUNT) {
            throw new VerificationException(
                    "Maximum resend limit reached. Only " + MAX_RESEND_COUNT + " resends are allowed.",
                    "MAX_RESEND_EXCEEDED"
            );
        }

        verification.setResendCount(verification.getResendCount() + 1);
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));

        String newToken = UUID.randomUUID().toString();
        verification.setVerificationLink("/api/v1/email-verification/confirm?token=" + newToken);
        tokenToVerificationId.put(newToken, verification.getId());

        return buildResponse(verification,
                "Verification email resent. Resend count: " + verification.getResendCount() + "/" + MAX_RESEND_COUNT);
    }

    public EmailVerificationResponse getStatus(String verificationId) {
        EmailVerification verification = verifications.get(verificationId);
        if (verification == null) {
            throw new VerificationNotFoundException("Verification not found for ID: " + verificationId);
        }
        return buildResponse(verification, "Current verification status: " + verification.getStatus());
    }

    private EmailVerificationResponse buildResponse(EmailVerification verification, String message) {
        EmailVerificationResponse response = new EmailVerificationResponse();
        response.setVerificationId(verification.getId());
        response.setRequestId(verification.getRequestId());
        response.setCorporateEmail(verification.getCorporateEmail());
        response.setStatus(verification.getStatus());
        response.setMessage(message);
        response.setResendCount(verification.getResendCount());
        response.setMaxResendAllowed(MAX_RESEND_COUNT);
        response.setExpiresAt(verification.getExpiresAt());
        return response;
    }
}
