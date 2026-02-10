package com.example.identityverification.controller;

import com.example.identityverification.dto.PhoneCodeVerifyRequest;
import com.example.identityverification.dto.PhoneVerificationRequest;
import com.example.identityverification.dto.PhoneVerificationResponse;
import com.example.identityverification.model.VerificationStatus;
import com.example.identityverification.service.PhoneVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(PhoneVerificationController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class PhoneVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhoneVerificationService phoneVerificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private PhoneVerificationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new PhoneVerificationResponse();
        sampleResponse.setVerificationId("b2c3d4e5-f6a7-8901-bcde-f12345678901");
        sampleResponse.setRequestId("REQ-2024-001");
        sampleResponse.setMobileNumber("****7890");
        sampleResponse.setStatus(VerificationStatus.SMS_SENT);
        sampleResponse.setMessage("6-digit verification code sent to ****7890. Code valid for 5 minutes.");
        sampleResponse.setAttemptCount(0);
        sampleResponse.setMaxAttempts(3);
        sampleResponse.setResendCount(0);
        sampleResponse.setMaxResendAllowed(1);
        sampleResponse.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void sendVerificationCode() throws Exception {
        when(phoneVerificationService.sendVerificationCode(any(PhoneVerificationRequest.class)))
                .thenReturn(sampleResponse);

        PhoneVerificationRequest request = new PhoneVerificationRequest("REQ-2024-001", "+1234567890");

        mockMvc.perform(post("/api/v1/phone-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationId").exists())
                .andExpect(jsonPath("$.status").value("SMS_SENT"))
                .andDo(document("phone-send",
                        requestFields(
                                fieldWithPath("requestId").description("Unique certificate request identifier"),
                                fieldWithPath("mobileNumber").description("User's registered mobile number in E.164 format")
                        ),
                        responseFields(
                                fieldWithPath("verificationId").description("Unique verification identifier"),
                                fieldWithPath("requestId").description("Certificate request identifier"),
                                fieldWithPath("mobileNumber").description("Masked mobile number"),
                                fieldWithPath("status").description("Verification status (SMS_SENT)"),
                                fieldWithPath("message").description("Human-readable status message"),
                                fieldWithPath("attemptCount").description("Number of code entry attempts made"),
                                fieldWithPath("maxAttempts").description("Maximum allowed attempts (3)"),
                                fieldWithPath("resendCount").description("Number of times code has been resent"),
                                fieldWithPath("maxResendAllowed").description("Maximum allowed resends (1)"),
                                fieldWithPath("expiresAt").description("Code expiry timestamp (5 minutes)")
                        )
                ));
    }

    @Test
    void verifyCode() throws Exception {
        PhoneVerificationResponse verifiedResponse = new PhoneVerificationResponse();
        verifiedResponse.setVerificationId("b2c3d4e5-f6a7-8901-bcde-f12345678901");
        verifiedResponse.setRequestId("REQ-2024-001");
        verifiedResponse.setMobileNumber("****7890");
        verifiedResponse.setStatus(VerificationStatus.SMS_VERIFIED);
        verifiedResponse.setMessage("Phone number verified successfully.");
        verifiedResponse.setAttemptCount(1);
        verifiedResponse.setMaxAttempts(3);
        verifiedResponse.setResendCount(0);
        verifiedResponse.setMaxResendAllowed(1);
        verifiedResponse.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(phoneVerificationService.verifyCode(any(PhoneCodeVerifyRequest.class)))
                .thenReturn(verifiedResponse);

        PhoneCodeVerifyRequest request = new PhoneCodeVerifyRequest(
                "b2c3d4e5-f6a7-8901-bcde-f12345678901", "123456");

        mockMvc.perform(post("/api/v1/phone-verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SMS_VERIFIED"))
                .andDo(document("phone-verify"));
    }

    @Test
    void resendCode() throws Exception {
        PhoneVerificationResponse resendResponse = new PhoneVerificationResponse();
        resendResponse.setVerificationId("b2c3d4e5-f6a7-8901-bcde-f12345678901");
        resendResponse.setRequestId("REQ-2024-001");
        resendResponse.setMobileNumber("****7890");
        resendResponse.setStatus(VerificationStatus.SMS_SENT);
        resendResponse.setMessage("New verification code sent. Resend count: 1/1");
        resendResponse.setAttemptCount(0);
        resendResponse.setMaxAttempts(3);
        resendResponse.setResendCount(1);
        resendResponse.setMaxResendAllowed(1);
        resendResponse.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(phoneVerificationService.resendCode(anyString()))
                .thenReturn(resendResponse);

        mockMvc.perform(post("/api/v1/phone-verification/{verificationId}/resend",
                        "b2c3d4e5-f6a7-8901-bcde-f12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resendCount").value(1))
                .andDo(document("phone-resend"));
    }

    @Test
    void getVerificationStatus() throws Exception {
        when(phoneVerificationService.getStatus(anyString()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/phone-verification/{verificationId}/status",
                        "b2c3d4e5-f6a7-8901-bcde-f12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SMS_SENT"))
                .andDo(document("phone-status"));
    }
}
