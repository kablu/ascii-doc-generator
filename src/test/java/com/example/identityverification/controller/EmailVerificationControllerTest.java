package com.example.identityverification.controller;

import com.example.identityverification.dto.EmailConfirmRequest;
import com.example.identityverification.dto.EmailVerificationRequest;
import com.example.identityverification.dto.EmailVerificationResponse;
import com.example.identityverification.model.VerificationStatus;
import com.example.identityverification.service.EmailVerificationService;
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
@WebMvcTest(EmailVerificationController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailVerificationService emailVerificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmailVerificationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new EmailVerificationResponse();
        sampleResponse.setVerificationId("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        sampleResponse.setRequestId("REQ-2024-001");
        sampleResponse.setCorporateEmail("john.doe@company.com");
        sampleResponse.setStatus(VerificationStatus.EMAIL_SENT);
        sampleResponse.setMessage("Verification email sent to john.doe@company.com");
        sampleResponse.setResendCount(0);
        sampleResponse.setMaxResendAllowed(3);
        sampleResponse.setExpiresAt(LocalDateTime.now().plusHours(24));
    }

    @Test
    void sendVerificationEmail() throws Exception {
        when(emailVerificationService.sendVerificationEmail(any(EmailVerificationRequest.class)))
                .thenReturn(sampleResponse);

        EmailVerificationRequest request = new EmailVerificationRequest("REQ-2024-001", "john.doe@company.com");

        mockMvc.perform(post("/api/v1/email-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationId").exists())
                .andExpect(jsonPath("$.status").value("EMAIL_SENT"))
                .andDo(document("email-send",
                        requestFields(
                                fieldWithPath("requestId").description("Unique certificate request identifier"),
                                fieldWithPath("corporateEmail").description("User's registered corporate email address")
                        ),
                        responseFields(
                                fieldWithPath("verificationId").description("Unique verification identifier"),
                                fieldWithPath("requestId").description("Certificate request identifier"),
                                fieldWithPath("corporateEmail").description("Corporate email address"),
                                fieldWithPath("status").description("Verification status (EMAIL_SENT)"),
                                fieldWithPath("message").description("Human-readable status message"),
                                fieldWithPath("resendCount").description("Number of times email has been resent"),
                                fieldWithPath("maxResendAllowed").description("Maximum allowed resends (3)"),
                                fieldWithPath("expiresAt").description("Verification link expiry timestamp (24 hours)")
                        )
                ));
    }

    @Test
    void confirmEmail() throws Exception {
        EmailVerificationResponse verifiedResponse = new EmailVerificationResponse();
        verifiedResponse.setVerificationId("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        verifiedResponse.setRequestId("REQ-2024-001");
        verifiedResponse.setCorporateEmail("john.doe@company.com");
        verifiedResponse.setStatus(VerificationStatus.EMAIL_VERIFIED);
        verifiedResponse.setMessage("Email verified successfully. Request marked as verified.");
        verifiedResponse.setResendCount(0);
        verifiedResponse.setMaxResendAllowed(3);
        verifiedResponse.setExpiresAt(LocalDateTime.now().plusHours(24));

        when(emailVerificationService.confirmEmail(any(EmailConfirmRequest.class)))
                .thenReturn(verifiedResponse);

        EmailConfirmRequest request = new EmailConfirmRequest("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

        mockMvc.perform(post("/api/v1/email-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EMAIL_VERIFIED"))
                .andDo(document("email-confirm"));
    }

    @Test
    void resendVerificationEmail() throws Exception {
        EmailVerificationResponse resendResponse = new EmailVerificationResponse();
        resendResponse.setVerificationId("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        resendResponse.setRequestId("REQ-2024-001");
        resendResponse.setCorporateEmail("john.doe@company.com");
        resendResponse.setStatus(VerificationStatus.EMAIL_SENT);
        resendResponse.setMessage("Verification email resent. Resend count: 1/3");
        resendResponse.setResendCount(1);
        resendResponse.setMaxResendAllowed(3);
        resendResponse.setExpiresAt(LocalDateTime.now().plusHours(24));

        when(emailVerificationService.resendVerificationEmail(anyString()))
                .thenReturn(resendResponse);

        mockMvc.perform(post("/api/v1/email-verification/{verificationId}/resend",
                        "a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resendCount").value(1))
                .andDo(document("email-resend"));
    }

    @Test
    void getVerificationStatus() throws Exception {
        when(emailVerificationService.getStatus(anyString()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/email-verification/{verificationId}/status",
                        "a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EMAIL_SENT"))
                .andDo(document("email-status"));
    }
}
