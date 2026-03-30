<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:6DB33F,100:00d9ff&height=120&section=header&text=QuickMail%20%E2%80%94%20Backend%20API&fontSize=36&fontColor=ffffff&fontAlignY=65&animation=fadeIn" />

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=700&size=20&pause=1000&color=6DB33F&center=true&vCenter=true&width=700&height=60&lines=Multi-Provider+AI+Fallback+System+%F0%9F%A4%96;Spring+Boot+REST+API+%E2%98%95;Load+Balancing+%2B+Auto+Failover+%E2%9A%A1;Language-Aware+Prompt+Engine+%F0%9F%87%AE%F0%9F%87%B3;Deployed+%26+Live+on+Railway+%F0%9F%9A%80" alt="Typing SVG" />

<br/>

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](#)
[![REST API](https://img.shields.io/badge/REST_API-FF6B6B?style=for-the-badge&logo=postman&logoColor=white)](#)
[![Railway](https://img.shields.io/badge/Live-Railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)](#)
[![AI](https://img.shields.io/badge/AI-Multi--Provider-4285F4?style=for-the-badge&logo=google&logoColor=white)](#)

</div>

---

## 📌 What This Project Does

> **QuickMail Backend** is the core AI engine behind the QuickMail Assistant Chrome Extension. It receives email context from Gmail, intelligently routes the request across multiple AI providers, and returns a human-like email draft — all in under 2 seconds.

```
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
| **AI Providers** | OpenAI GPT · Google Gemini · Groq · OpenRouter |
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
        │  POST /api/email/generate
        ▼
┌──────────────────────────────────┐
│         Spring Boot API          │
│                                  │
│  ┌───────────────────────────┐   │
│  │     Request Middleware    │   │
│  │  CORS · Logging · Validate│   │
│  └────────────┬──────────────┘   │
│               │                  │
│  ┌────────────▼──────────────┐   │
│  │      Prompt Builder       │   │
│  │  Language Detection +     │   │
│  │  Context Formatting       │   │
│  └────────────┬──────────────┘   │
│               │                  │
│  ┌────────────▼──────────────┐   │
│  │    AI Provider Router     │   │
│  │  Load Balancer + Fallback │   │
│  └────────────┬──────────────┘   │
│               │                  │
│   ┌───────────┼───────────┐      │
│   ▼           ▼           ▼      │
│ OpenAI     Gemini     Groq /     │
│  GPT                 OpenRouter  │
└──────────────────────────────────┘
        │
        ▼
  Generated Draft → Gmail ✅
```

### Request Flow

```
Request → Middleware → Prompt Builder → AI Router → Provider → Response
```

---

## 📁 Project Structure

```
quickmail-backend/
├── controller/
│   └── EmailController.java          # REST endpoints
├── service/
│   ├── EmailGenerationService.java   # Core generation logic
│   ├── PromptBuilderService.java     # Language-aware prompt engine
│   └── ai/
│       ├── AIProviderRouter.java     # Load balancer + fallback chain
│       ├── OpenAIProvider.java
│       ├── GeminiProvider.java
│       ├── GroqProvider.java
│       └── OpenRouterProvider.java
├── middleware/
│   ├── CorsConfig.java
│   └── RequestLoggingFilter.java
├── model/
│   ├── EmailRequest.java
│   └── EmailResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── AIProviderException.java
└── QuickMailApplication.java
```

---

## 📡 API Reference

### `POST /api/email/generate`

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
  "error": "All AI providers are currently unavailable.",
  "code": "PROVIDER_EXHAUSTED"
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
| `422` | Invalid mode or language |
| `503` | All AI providers unavailable |
| `500` | Internal server error |

</div>

All errors return consistent structured format — no raw stack traces exposed to client.

---

## 🗺️ Future Roadmap

### v2 — Auth & Users
- [ ] JWT-based authentication
- [ ] Per-user usage tracking
- [ ] User preferences (language, provider)

### v3 — Database & History
- [ ] PostgreSQL integration
- [ ] Email generation history
- [ ] Saved prompt templates

### v4 — Advanced Features
- [ ] Tone selector (formal · casual · assertive)
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
