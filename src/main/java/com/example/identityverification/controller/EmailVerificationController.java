package com.example.identityverification.controller;

import com.example.identityverification.dto.EmailConfirmRequest;
import com.example.identityverification.dto.EmailVerificationRequest;
import com.example.identityverification.dto.EmailVerificationResponse;
import com.example.identityverification.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email-verification")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<EmailVerificationResponse> sendVerificationEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        EmailVerificationResponse response = emailVerificationService.sendVerificationEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<EmailVerificationResponse> confirmEmail(
            @Valid @RequestBody EmailConfirmRequest request) {
        EmailVerificationResponse response = emailVerificationService.confirmEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{verificationId}/resend")
    public ResponseEntity<EmailVerificationResponse> resendVerificationEmail(
            @PathVariable String verificationId) {
        EmailVerificationResponse response = emailVerificationService.resendVerificationEmail(verificationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{verificationId}/status")
    public ResponseEntity<EmailVerificationResponse> getVerificationStatus(
            @PathVariable String verificationId) {
        EmailVerificationResponse response = emailVerificationService.getStatus(verificationId);
        return ResponseEntity.ok(response);
    }
}
