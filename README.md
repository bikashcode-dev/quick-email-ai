<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:6DB33F,100:00d9ff&height=120&section=header&text=QuickMail%20%E2%80%94%20Backend%20API&fontSize=36&fontColor=ffffff&fontAlignY=65&animation=fadeIn" />

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=700&size=20&pause=1000&color=6DB33F&center=true&vCenter=true&width=700&height=60&lines=Multi-Provider+AI+Fallback+System+%F0%9F%A4%96;Spring+Boot+REST+API+%E2%98%95;OTP+Email+Auth+%7C+JWT+Security+%F0%9F%94%90;MongoDB+%7C+Register+%7C+Login+%7C+Verify+%F0%9F%A7%A9;Load+Balancing+%2B+Auto+Failover+%E2%9A%A1;Language-Aware+Prompt+Engine+%F0%9F%87%AE%F0%9F%87%B3;Deployed+%26+Live+on+Railway+%F0%9F%9A%80" alt="Typing SVG" />

<br/>

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](#)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](#)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](#)
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](#)
[![Railway](https://img.shields.io/badge/Live-Railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)](#)
[![AI](https://img.shields.io/badge/AI-Multi--Provider-4285F4?style=for-the-badge&logo=google&logoColor=white)](#)

</div>

---

## 📌 What This Project Does

> **QuickMail Backend** is the core AI engine behind the QuickMail Assistant Chrome Extension. Users must **register and verify their email via OTP**, then **sign in to get a JWT token** — only then can they use the extension. It receives email context from Gmail, intelligently routes the request across multiple AI providers, and returns a human-like email draft — all in under 2 seconds.

```
✅ OTP-based Email Registration — verify your email before using the extension
✅ Secure Sign In — JWT token issued after login, required for all API calls
✅ MongoDB — stores users, OTP records, and session data
✅ Spring Security + JWT — every protected endpoint requires valid token
✅ Multi-provider AI — OpenAI · Gemini · Groq · OpenRouter with auto-fallback
✅ Load balancing across providers — no single point of failure
✅ Language-aware — detects and responds in English, Hindi, or Hinglish
✅ Layered architecture — clean separation of concerns
✅ Global exception handling — structured, consistent error responses
✅ Live & deployed on Railway
```

---

## 🛠️ Tech Stack

<div align="center">

| Layer | Technology |
|:---|:---|
| **Language** | Java 17 |
| **Framework** | Spring Boot |
| **Security** | Spring Security · JWT · OTP Email Auth |
| **Database** | MongoDB |
| **AI Providers** | OpenAI GPT · Google Gemini · Groq · OpenRouter |
| **Email Service** | JavaMailSender (SMTP) — OTP delivery |
| **Deployment** | Railway |
| **Build Tool** | Maven |
| **API Style** | REST |
| **Architecture** | Layered — Controller → Service → Provider |

</div>

---

## 🏗️ System Architecture

```
Chrome Extension (Client)
        │
        │  Step 1: Register → Verify OTP → Sign In → Get JWT
        │  Step 2: Use JWT for all AI email requests
        ▼
┌──────────────────────────────────────────┐
│            Spring Boot API               │
│                                          │
│  ┌───────────────────────────────────┐   │
│  │      Spring Security Filter       │   │
│  │   JWT Validation on every request │   │
│  └──────────────┬────────────────────┘   │
│                 │                        │
│   ┌─────────────┴──────────────────┐    │
│   │                                │    │
│   ▼                                ▼    │
│ AUTH FLOW                    AI FLOW    │
│ ──────────                   ───────    │
│ Register                     Prompt     │
│ Send OTP → Email             Builder    │
│ Verify OTP                      │      │
│ Sign In → JWT               AI Router  │
│                                  │      │
│   MongoDB ◄──────────────────────┤      │
│  (users · otps · sessions)       │      │
│                            OpenAI /     │
│                            Gemini /     │
│                            Groq /       │
│                            OpenRouter   │
└──────────────────────────────────────────┘
        │
        ▼
  Generated Draft → Gmail ✅
```

### Full Request Flow

```
[Register] → [OTP Email Sent] → [Verify OTP] → [Sign In] → [JWT Token]
                                                                  │
                                                                  ▼
                                          [POST /api/email/generate + Bearer JWT]
                                                                  │
                                          Middleware → Prompt Builder → AI Router → Provider → Response
```

---

## 📁 Project Structure

```
quickmail-backend/
├── controller/
│   ├── AuthController.java               # Register · Verify OTP · Sign In
│   └── EmailController.java              # AI email generation (protected)
├── service/
│   ├── auth/
│   │   ├── AuthService.java              # Register / login logic
│   │   ├── OtpService.java               # OTP generation, validation, expiry
│   │   └── JwtService.java               # JWT create & validate
│   ├── EmailGenerationService.java       # Core AI generation logic
│   ├── PromptBuilderService.java         # Language-aware prompt engine
│   └── ai/
│       ├── AIProviderRouter.java         # Load balancer + fallback chain
│       ├── OpenAIProvider.java
│       ├── GeminiProvider.java
│       ├── GroqProvider.java
│       └── OpenRouterProvider.java
├── model/
│   ├── User.java                         # MongoDB document — users
│   ├── OtpRecord.java                    # MongoDB document — OTP + expiry
│   ├── EmailRequest.java
│   └── EmailResponse.java
├── repository/
│   ├── UserRepository.java               # MongoDB CRUD for users
│   └── OtpRepository.java               # MongoDB CRUD for OTPs
├── security/
│   ├── SecurityConfig.java               # Spring Security config
│   ├── JwtAuthFilter.java                # JWT filter — validates every request
│   └── UserDetailsServiceImpl.java
├── middleware/
│   ├── CorsConfig.java
│   └── RequestLoggingFilter.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── AIProviderException.java
└── QuickMailApplication.java
```

---

## 🔐 Auth Flow — Register · Verify · Sign In

### Step 1 — Register

```
POST /api/auth/register
```

```json
{
  "name": "Bikash Kumar",
  "email": "bikashcode@gmail.com",
  "password": "yourPassword"
}
```

> Account created → OTP generated → sent to email via SMTP

**Response — 200**
```json
{
  "success": true,
  "message": "OTP sent to bikashcode@gmail.com. Please verify to activate your account."
}
```

---

### Step 2 — Verify OTP

```
POST /api/auth/verify-otp
```

```json
{
  "email": "bikashcode@gmail.com",
  "otp": "847291"
}
```

> OTP matched + not expired → account activated in MongoDB

**Response — 200**
```json
{
  "success": true,
  "message": "Email verified successfully. You can now sign in."
}
```

**OTP Rules:**
```
✅ 6-digit OTP — randomly generated
✅ Expires in 10 minutes (TTL enforced)
✅ Single-use — invalidated after successful verification
✅ Resend available via /api/auth/resend-otp
```

---

### Step 3 — Sign In

```
POST /api/auth/signin
```

```json
{
  "email": "bikashcode@gmail.com",
  "password": "yourPassword"
}
```

> Credentials verified → JWT token issued → use this token for all API calls

**Response — 200**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": "30-Day"
}
```

---

### Step 4 — Use Extension with JWT

Every request to protected endpoints **must include:**

```
Authorization: Bearer <your_jwt_token>
```

> Extension stores JWT after sign in → attaches it automatically to every AI request

---

## 📡 Auth API Reference

| Endpoint | Method | Auth Required | Description |
|:---|:---|:---|:---|
| `/api/auth/register` | POST | ❌ | Create account, send OTP |
| `/api/auth/verify-otp` | POST | ❌ | Verify OTP, activate account |
| `/api/auth/resend-otp` | POST | ❌ | Resend OTP to email |
| `/api/auth/signin` | POST | ❌ | Login, get JWT token |
| `/api/email/generate` | POST | ✅ JWT | Generate AI email reply |

---

## 📡 AI Email API Reference

### `POST /api/email/generate` *(JWT Required)*

```json
{
  "mode": "reply",
  "context": "Original email thread...",
  "instruction": "Reply politely declining the meeting",
  "language": "auto"
}
```

| Field | Required | Description |
|:---|:---|:---|
| `mode` | ✅ | `reply` or `compose` |
| `context` | reply mode | Existing email thread |
| `instruction` | compose mode | What to write |
| `language` | ❌ | `en` · `hi` · `hinglish` · `auto` |

**Success — 200**
```json
{
  "success": true,
  "draft": "Thank you for reaching out...",
  "provider": "gemini",
  "language_detected": "en"
}
```

**Error — 4xx/5xx**
```json
{
  "success": false,
  "error": "Provider currently unavailable.",
  "code": "PROVIDER_EXHAUSTED"
}
```

---

## 🗄️ MongoDB Collections

<div align="center">

| Collection | Purpose |
|:---|:---|
| `users` | Stores name, email, hashed password, verified status |
| `otp_records` | Stores OTP, email, expiry timestamp, used flag |

</div>

```
users collection:
{
  "_id": ObjectId,
  "name": "Bikash Kumar",
  "email": "bikashcode@gmail.com",
  "password": "<bcrypt_hash>",
  "verified": true,
  "createdAt": ISODate
}

otp_records collection:
{
  "_id": ObjectId,
  "email": "bikashcode@gmail.com",
  "otp": "847291",
  "expiresAt": ISODate,
  "used": false
}
```

---

## 🤖 Multi-Provider AI Fallback

> If one provider fails — rate limit, timeout, or error — the system automatically tries the next. Zero manual intervention needed.

```
Primary  →  OpenAI GPT
               ↓ fail
Fallback 1 →  Google Gemini
               ↓ fail
Fallback 2 →  Groq
               ↓ fail
Fallback 3 →  OpenRouter
               ↓ all fail
           503 — Unavailable
```

**Load Balancing:**
- Requests distributed across all healthy providers
- Provider health tracked in real-time
- Degraded providers skipped automatically
- Prevents rate limit exhaustion on any single API

---

## 🌐 Language Support

<div align="center">

| Input | Output Behavior |
|:---|:---|
| **English** | Professional English email |
| **Hindi** (Devanagari) | Full Hindi email response |
| **Hinglish** | Detects mixed input, matches output |
| **auto** | Detects language, mirrors it in reply |

</div>

> Detection runs in `PromptBuilderService` before the AI call — output always matches user's language intent.

---

## ⚠️ Error Handling

<div align="center">

| Code | Scenario |
|:---|:---|
| `400` | Missing required fields |
| `401` | Invalid or expired JWT token |
| `403` | Email not verified — OTP pending |
| `409` | Email already registered |
| `410` | OTP expired |
| `422` | Invalid mode or language |
| `503` | All AI providers unavailable |
| `500` | Internal server error |

</div>

All errors return consistent structured format — no raw stack traces exposed to client.

---

## 🗺️ Future Roadmap

### v2 — User Features
- [ ] Per-user usage tracking & limits
- [ ] User preferences (language, default tone, provider)
- [ ] Password reset via OTP
- [ ] PDF reader and Exel reader
### v3 — History & Templates
- [ ] Email generation history per user
- [ ] Saved prompt templates

### v4 — Advanced Features
- [ ] Email summarization endpoint
- [ ] Async generation with webhooks

---

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:00d9ff,100:6DB33F&height=80&section=footer&animation=fadeIn" />

**Built by [Bikash Kumar](https://github.com/bikashcode-dev)**

[![GitHub](https://img.shields.io/badge/GitHub-bikashcode--dev-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/bikashcode-dev)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Bikash%20Sah-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/bikash-sah-java)

⭐ *"Build real projects, not just code."*

</div>
