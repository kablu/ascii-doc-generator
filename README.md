# Identity Verification Service - AsciiDoc Generator

A Spring Boot project that implements identity verification REST APIs and generates PDF documentation from AsciiDoc with Mermaid sequence and state diagrams.

## Overview

This project converts static identity verification requirements (email and phone/SMS verification for certificate issuance) into a fully documented Spring Boot service with:

- Working REST APIs for email and phone verification workflows
- AsciiDoc documentation with Mermaid diagrams rendered into PDF
- Spring REST Docs integration for auto-generated API request/response snippets

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.5** (Web, Validation)
- **Spring REST Docs** (MockMvc-based API documentation)
- **AsciiDoctor Gradle Plugin 4.0.2** (HTML + PDF generation)
- **AsciidoctorJ Diagram 2.3.0** (Mermaid diagram rendering)
- **Mermaid CLI (mmdc)** (diagram-to-image conversion)
- **Gradle 8.7**

## Project Structure

```
├── build.gradle                          # Gradle build config with AsciiDoc + diagram plugins
├── settings.gradle
├── gradlew.bat
├── src/
│   ├── main/java/com/example/identityverification/
│   │   ├── IdentityVerificationApplication.java
│   │   ├── controller/
│   │   │   ├── EmailVerificationController.java
│   │   │   └── PhoneVerificationController.java
│   │   ├── service/
│   │   │   ├── EmailVerificationService.java
│   │   │   └── PhoneVerificationService.java
│   │   ├── model/
│   │   │   ├── VerificationStatus.java
│   │   │   ├── EmailVerification.java
│   │   │   └── PhoneVerification.java
│   │   ├── dto/
│   │   │   ├── EmailVerificationRequest.java
│   │   │   ├── EmailVerificationResponse.java
│   │   │   ├── EmailConfirmRequest.java
│   │   │   ├── PhoneVerificationRequest.java
│   │   │   ├── PhoneVerificationResponse.java
│   │   │   ├── PhoneCodeVerifyRequest.java
│   │   │   └── ErrorResponse.java
│   │   └── exception/
│   │       ├── VerificationException.java
│   │       ├── VerificationNotFoundException.java
│   │       └── GlobalExceptionHandler.java
│   ├── main/resources/
│   │   └── application.yml
│   ├── docs/asciidoc/
│   │   └── index.adoc                    # Main AsciiDoc (requirements + API + diagrams)
│   └── test/java/.../controller/
│       ├── EmailVerificationControllerTest.java
│       └── PhoneVerificationControllerTest.java
└── build/
    ├── docs/asciidoc/index.html          # Generated HTML
    ├── docs/asciidocPdf/index.pdf        # Generated PDF
    └── generated-snippets/               # REST Docs snippets
```

## Requirements Covered

### 3.1 Email Verification

| # | Criterion | Priority |
|---|-----------|----------|
| AC-1 | System must send verification email to user's registered corporate email address | MUST |
| AC-2 | Email must contain unique verification link valid for 24 hours | MUST |
| AC-3 | End Entity/User must click the link to verify email ownership | MUST |
| AC-4 | System must allow resending verification email up to 3 times | MUST |
| AC-5 | System must mark request as verified after successful email confirmation | MUST |
| AC-6 | Unverified requests must not proceed to approval | MUST |

### 3.2 Phone/SMS Verification

| # | Criterion | Priority |
|---|-----------|----------|
| AC-1 | System must send 6-digit verification code to user's registered mobile number | MUST |
| AC-2 | Code must be valid for 5 minutes | MUST |
| AC-3 | End Entity/User must enter code within time limit | MUST |
| AC-4 | System must allow maximum 3 attempts to enter correct code | MUST |
| AC-5 | System must allow resending code once per request | MUST |
| AC-6 | Failed verification must prevent request from proceeding | MUST |

## REST API Endpoints

### Email Verification

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/email-verification/send` | Send verification email |
| POST | `/api/v1/email-verification/confirm` | Confirm email via token |
| POST | `/api/v1/email-verification/{id}/resend` | Resend email (max 3) |
| GET | `/api/v1/email-verification/{id}/status` | Check verification status |

### Phone/SMS Verification

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/phone-verification/send` | Send 6-digit SMS code |
| POST | `/api/v1/phone-verification/verify` | Verify code (max 3 attempts) |
| POST | `/api/v1/phone-verification/{id}/resend` | Resend code (max 1) |
| GET | `/api/v1/phone-verification/{id}/status` | Check verification status |

## Diagrams Included (Mermaid)

The generated PDF contains 6 Mermaid diagrams:

1. **Email Verification Sequence Diagram** - Full flow: send, confirm, resend with alt paths
2. **Email Verification State Diagram** - States: PENDING → EMAIL_SENT → EMAIL_VERIFIED / EXPIRED / FAILED
3. **Phone/SMS Verification Sequence Diagram** - Full flow: send code, verify, resend with alt paths
4. **Phone/SMS Verification State Diagram** - States: PENDING → SMS_SENT → SMS_VERIFIED / EXPIRED / FAILED
5. **Combined Verification Flow** - End-to-end sequence for both email + phone verification
6. **Combined State Machine** - Nested state diagram showing both verification workflows

## Prerequisites

- **Java 17+**
- **Node.js** (for mermaid-cli)
- **Mermaid CLI**: `npm install -g @mermaid-js/mermaid-cli`

## Build Commands

```bash
# Run tests (generates REST Docs snippets)
gradlew.bat test

# Generate HTML documentation
gradlew.bat asciidoctor

# Generate PDF documentation
gradlew.bat asciidoctorPdf

# Run the Spring Boot application
gradlew.bat bootRun

# Full build (test + HTML + PDF + bootJar)
gradlew.bat build
```

## Output

- **HTML**: `build/docs/asciidoc/index.html`
- **PDF**: `build/docs/asciidocPdf/index.pdf`
- **REST Docs snippets**: `build/generated-snippets/`

## Gradle Configuration Highlights

- `org.asciidoctor.jvm.convert` - HTML generation
- `org.asciidoctor.jvm.pdf` - PDF generation
- `asciidoctorj-diagram:2.3.0` - Mermaid diagram support via `mmdc`
- `spring-restdocs-mockmvc` - Auto-generated API snippets from tests
- `spring-restdocs-asciidoctor` - Integrates snippets into AsciiDoc
