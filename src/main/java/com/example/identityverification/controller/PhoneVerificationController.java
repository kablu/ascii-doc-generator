package com.example.identityverification.controller;

import com.example.identityverification.dto.PhoneCodeVerifyRequest;
import com.example.identityverification.dto.PhoneVerificationRequest;
import com.example.identityverification.dto.PhoneVerificationResponse;
import com.example.identityverification.service.PhoneVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/phone-verification")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    public PhoneVerificationController(PhoneVerificationService phoneVerificationService) {
        this.phoneVerificationService = phoneVerificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<PhoneVerificationResponse> sendVerificationCode(
            @Valid @RequestBody PhoneVerificationRequest request) {
        PhoneVerificationResponse response = phoneVerificationService.sendVerificationCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<PhoneVerificationResponse> verifyCode(
            @Valid @RequestBody PhoneCodeVerifyRequest request) {
        PhoneVerificationResponse response = phoneVerificationService.verifyCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{verificationId}/resend")
    public ResponseEntity<PhoneVerificationResponse> resendCode(
            @PathVariable String verificationId) {
        PhoneVerificationResponse response = phoneVerificationService.resendCode(verificationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{verificationId}/status")
    public ResponseEntity<PhoneVerificationResponse> getVerificationStatus(
            @PathVariable String verificationId) {
        PhoneVerificationResponse response = phoneVerificationService.getStatus(verificationId);
        return ResponseEntity.ok(response);
    }
}
