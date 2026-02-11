# End Entity/User Login & MFA - Requirement Analysis

## 1. Requirement Summary

### 1.1 End Entity/User Login
The login module authenticates End Entity/Users using corporate Active Directory (AD) credentials. The system does not store passwords locally; instead, it validates credentials in real-time against the AD server via LDAP bind. Upon successful authentication, users are redirected to a role-appropriate dashboard.

### 1.2 Multi-Factor Authentication (MFA)
MFA adds a second verification layer after successful AD login. The system supports two MFA methods:
- **TOTP** (Time-based One-Time Password) via authenticator apps (Google/Microsoft Authenticator)
- **Email OTP** sent to the user's registered corporate email as a fallback

First-time users must complete MFA setup (scan QR code, save recovery codes) before accessing the system.

---

## 2. Acceptance Criteria Breakdown

### 2.1 Login Acceptance Criteria

| ID | Criterion | Priority | Implementation Notes |
|----|-----------|----------|---------------------|
| AC-1 | AD username/password authentication | MUST | LDAP bind against corporate AD |
| AC-2 | Real-time AD credential validation | MUST | No local credential caching |
| AC-3 | Clear error messages on failure | MUST | Differentiate: invalid creds, locked, disabled |
| AC-4 | Role-based dashboard redirect | MUST | Map AD groups to application roles |
| AC-5 | Account lockout (5 attempts / 30 min) | MUST | Server-side attempt tracking per username |
| AC-6 | Audit logging (all attempts) | MUST | Log timestamp, IP, username, result |
| AC-7 | Session inactivity timeout (30 min) | SHOULD | Sliding window on JWT + Redis TTL |
| AC-8 | Max 3 concurrent sessions | SHOULD | Session store counting per user |

### 2.2 MFA Acceptance Criteria

| ID | Criterion | Priority | Implementation Notes |
|----|-----------|----------|---------------------|
| AC-1 | MFA prompt after successful AD auth | MUST | Intermediate session state (pre-MFA) |
| AC-2 | TOTP via authenticator app | MUST | RFC 6238 HMAC-SHA1, 30-sec window |
| AC-3 | Email OTP as fallback method | MUST | 6-digit code, 5-min TTL |
| AC-4 | 6-digit code, 30s/5min validity | MUST | TOTP window: +/- 1 drift, email: DB TTL |
| AC-5 | Max 3 MFA attempts per session | MUST | Counter in session store |
| AC-6 | Session termination on 3 failures | MUST | Invalidate pre-auth token |
| AC-7 | Email OTP resend (max 1) | SHOULD | Resend counter in session |
| AC-8 | Switch MFA method during prompt | SHOULD | UI toggle between TOTP/email |
| AC-9 | Mandatory MFA setup on first login | MUST | Check MFA config status on each login |
| AC-10 | 8 recovery codes during setup | MUST | Bcrypt hashed, single-use |

---

## 3. Architecture Analysis

### 3.1 Component Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌──────────────────────┐
│   Web Browser    │────▶│   API Gateway   │────▶│  Auth Service        │
│   (Client)       │     │  (Rate Limiter) │     │  ├─ Login Controller │
└─────────────────┘     └─────────────────┘     │  ├─ MFA Controller   │
                                                 │  ├─ Auth Service     │
                                                 │  ├─ MFA Service      │
                                                 │  ├─ TOTP Engine      │
                                                 │  └─ Session Manager  │
                                                 └──────────┬───────────┘
                                                            │
                                          ┌─────────────────┼─────────────────┐
                                          │                 │                 │
                                     ┌────▼────┐     ┌─────▼─────┐    ┌─────▼─────┐
                                     │ Active  │     │  Redis    │    │  Audit    │
                                     │Directory│     │ (Session) │    │   Log     │
                                     └─────────┘     └───────────┘    └───────────┘
```

### 3.2 Technology Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Authentication Backend | Active Directory (LDAP) | Corporate requirement - centralized identity |
| TOTP Implementation | RFC 6238 (HMAC-SHA1) | Industry standard, wide authenticator app support |
| Session Store | Redis | Fast read/write, TTL support, distributed |
| Token Format | JWT (RS256) | Stateless verification, role claims embedded |
| Secret Encryption | AES-256 | Industry standard for at-rest encryption |
| OTP Hashing | SHA-256 (salted) | Short-lived codes, fast verification needed |
| Recovery Code Hashing | Bcrypt | Long-lived, needs brute-force resistance |

### 3.3 Security Measures

1. **LDAP Injection Prevention**: Username input sanitized before AD query
2. **Brute Force Protection**: 5-attempt lockout + API rate limiting (10 req/min/IP)
3. **Token Security**: httpOnly secure cookies, RS256 JWT signing
4. **Secret Storage**: TOTP secrets AES-256 encrypted, OTPs SHA-256 hashed, recovery codes bcrypt hashed
5. **Transport Security**: TLS 1.2+ enforced on all endpoints
6. **Session Management**: Max 3 concurrent sessions, 30-min inactivity timeout

---

## 4. Validation Analysis

### 4.1 Client-Side Validations

| Field | Rules | Purpose |
|-------|-------|---------|
| Username | Required, 3-50 chars, alphanumeric + dots/underscores | Prevent invalid AD queries, UX feedback |
| Password | Required, 8-128 chars, non-blank | Prevent unnecessary server round-trips |
| MFA Code | Required, exactly 6 digits, numeric only | Format validation before server call |
| Recovery Code | Required, 8 alphanumeric chars | Format validation before server call |

### 4.2 Server-Side Validations

| Layer | Validations |
|-------|-------------|
| API Gateway | Rate limiting, request size limits, TLS enforcement |
| Auth Controller | Input format validation, LDAP injection sanitization |
| Auth Service | AD bind verification, lockout check, session limit check |
| MFA Service | Code verification (TOTP/OTP), attempt tracking, expiry check |
| Session Manager | Token validity, session concurrency, inactivity timeout |

---

## 5. API Flow Analysis

### 5.1 Login Flow (Happy Path)
1. User submits credentials → `POST /api/v1/auth/login`
2. Server validates against AD → returns `sessionToken` + `mfaRequired: true`
3. Client checks MFA status → `GET /api/v1/mfa/status`
4. User enters TOTP code → `POST /api/v1/mfa/verify`
5. Server validates TOTP → returns `accessToken` + `dashboardUrl`
6. Client redirects to role-appropriate dashboard

### 5.2 First Login Flow (MFA Setup)
1. User submits credentials → `POST /api/v1/auth/login`
2. MFA status check returns setup required → `GET /api/v1/mfa/status` (403)
3. User initiates TOTP setup → `POST /api/v1/mfa/setup`
4. Server returns QR code + 8 recovery codes
5. User scans QR, enters verification code → `POST /api/v1/mfa/setup/verify`
6. MFA configured → access granted

### 5.3 Error Scenarios
- **Invalid credentials**: 401 with generic message (no username/password distinction for security)
- **Account locked**: 423 with lockout expiry time
- **Rate limited**: 429 with retry-after hint
- **MFA code invalid**: 401 with remaining attempts count
- **MFA attempts exhausted**: 423 with redirect to login
- **Recovery code invalid**: 401 (no attempt count disclosed)

---

## 6. Diagrams Included

The AsciiDoc PDF (`docs/end-entity/index.adoc`) contains the following Mermaid diagrams:

| # | Diagram | Type | Description |
|---|---------|------|-------------|
| 1 | Login Sequence Diagram | sequenceDiagram | Full login flow with AD validation, rate limiting, lockout, session management |
| 2 | Login State Diagram | stateDiagram-v2 | States: Unauthenticated → Validating → Authenticated with all error paths |
| 3 | Login Block Diagram | flowchart | Component architecture: Client → Gateway → Auth Service → External Systems |
| 4 | MFA Sequence Diagram | sequenceDiagram | MFA setup, TOTP verification, email OTP, recovery code flows |
| 5 | MFA State Diagram | stateDiagram-v2 | States: LoginCompleted → MFAChallengeIssued → MFAVerified/MFAFailed |
| 6 | MFA Block Diagram | flowchart | Component architecture: Client → Gateway → MFA Service → External Systems |

---

## 7. Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| AD server unavailability | Users cannot login | Implement AD failover with secondary LDAP server |
| TOTP clock drift | Valid codes rejected | Allow +/- 1 window tolerance (30 seconds) |
| Email OTP delivery delay | Users cannot complete MFA in time | 5-minute TTL provides buffer; allow method switching |
| Recovery code exhaustion | Account lockout if authenticator lost | Warn when <= 2 codes remaining; admin reset option |
| Brute force on MFA | Code guessing | 3-attempt limit + session termination |
| Session hijacking | Unauthorized access | httpOnly secure cookies, short JWT TTL, token rotation |

---

## 8. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Login response time | < 2 seconds (including AD round-trip) |
| MFA verification time | < 1 second |
| Availability | 99.9% uptime |
| Concurrent users | 10,000+ simultaneous sessions |
| Audit log retention | Minimum 1 year |
| Password policy | Delegated to Active Directory |
