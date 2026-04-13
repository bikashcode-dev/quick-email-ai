# QuickMail Backend

QuickMail Backend is the Spring Boot API behind the QuickMail Gmail extension. It handles authentication, OTP verification, JWT-protected access, AI email draft generation, and provider fallback.

This project is designed as a production-style backend for a real user workflow:

<div align="center">

## Highlights

- Spring Boot 3 + Java 17
- Spring Security with stateless JWT authentication
- OTP-based account verification
- MongoDB persistence
- AI email generation with fallback strategy
- Railway-ready deployment setup
- Chrome extension integration

## What The Backend Does

The backend is responsible for:

- sending OTPs for email verification
- verifying OTPs and activating accounts
- allowing password-based login after verification
- issuing JWT tokens for protected API access
- receiving email drafting requests from the frontend
- building safe, structured prompts
- routing generation requests through multiple AI providers
- returning generated email content to the extension

## High-Level Flow

```text
Chrome Extension
   -> OTP / Login
   -> Receives JWT
   -> Sends protected draft request
   -> Backend validates token
   -> Backend builds prompt
   -> Backend calls AI provider(s)
   -> Draft returned to Gmail
```

## Authentication Flow

### 1. Request OTP

The user provides an email address.

The backend:

- normalizes the email
- invalidates older active OTPs
- generates a fresh OTP
- stores OTP metadata in MongoDB
- sends the OTP through configured mail delivery

### 2. Verify OTP

The backend:

- checks whether the OTP exists
- checks whether it is expired
- checks whether it was already used
- marks the user as verified
- returns a JWT token

### 3. Password Login

After verification, the user can optionally use password login.

The backend:

- validates the account
- checks the password hash
- issues a JWT token

## Email Generation Flow

Once the user is authenticated, the frontend sends a protected request to generate a draft.

The backend:

- validates JWT
- validates request payload
- builds a prompt based on mode and instruction
- sends generation through the AI orchestration layer
- returns generated text and provider metadata

## AI Fallback Strategy

The system is designed so one provider failure does not stop the product.

Current fallback order:

1. Groq
2. Gemini
3. OpenRouter

If one provider fails or returns an unusable response, the backend automatically tries the next provider.

## Security Approach

- stateless authentication with JWT
- protected API routes
- BCrypt password hashing
- OTP invalidation and expiry checks
- request validation before generation

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- MongoDB
- Maven
- WebClient
- JWT
- Railway

## Project Structure

```text
src/main/java/com/email/emailgen
+-- config
+-- controller
+-- dto
+-- exception
+-- model
+-- repository
+-- security
+-- service
�   +-- auth
�   +-- impl
�   +-- orchestrator
+-- EmailgenApplication.java
```

## Running Locally

```powershell
cmd /c "call mvnw.cmd spring-boot:run"
```

## Running Tests

```powershell
cmd /c "call mvnw.cmd test"
```

## Deployment Notes

- the backend is structured for deployment on Railway
- production configuration should come from environment variables
- local-only secrets should never be committed
- protected routes depend on valid JWT tokens

## Why This Project Is Strong

This backend is a strong portfolio/interview project because it combines:

- real authentication flow
- secure API design
- frontend-backend integration
- AI orchestration
- fallback handling
- production-style deployment thinking

## Future Improvements

- per-user usage tracking
- better observability and metrics
- refresh-token strategy
- async processing for higher traffic
- richer prompt/history management
