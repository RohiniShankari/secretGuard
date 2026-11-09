# SecretGuard 🔐

**An Explainable GitHub Repository Secret Scanner**

[![Live Demo](https://img.shields.io/badge/Live-Demo-brightgreen)](https://secret-guard-5c3kck4g5-sankari.vercel.app/)

**Live Application:** https://secret-guard-5c3kck4g5-sankari.vercel.app/

SecretGuard is a Java Spring Boot-based security tool that scans public GitHub repositories to identify potentially exposed secrets such as API keys, access tokens, passwords, and private keys.

Unlike basic pattern-matching scanners, SecretGuard combines multiple detection techniques to improve detection quality, explain why a value was flagged, and analyze potential exposures in Git commit history.

## 🚨 Problem

Developers sometimes accidentally commit sensitive information such as API keys, passwords, and access tokens into Git repositories.

Even when these secrets are removed from the latest version of a project, they may still exist in Git history. Manually identifying these exposures is time-consuming, and simple pattern-based detection can generate false positives.

## 💡 Solution

SecretGuard scans GitHub repositories using a combination of pattern matching, contextual analysis, and entropy-based detection.

It provides masked findings with explanations, confidence scores, and risk classifications. Its Git history analysis can also identify secrets introduced in earlier commits, even if they are no longer present in the current files.

## ✨ Key Features

- **GitHub Repository Scanning:** Scan public GitHub repositories using their repository URLs.
- **Pattern-Based Detection:** Detect common secret formats, including AWS access keys, GitHub tokens, and private key headers.
- **Contextual Analysis:** Analyze sensitive keywords around potential secrets.
- **Entropy Analysis:** Identify potentially sensitive values based on randomness and unpredictability.
- **Placeholder Detection:** Suppress common dummy values and placeholder credentials.
- **Confidence Scoring:** Assign confidence scores using multiple detection signals.
- **Risk Classification:** Categorize findings based on severity and confidence.
- **Secret Masking:** Mask detected values in scan results.
- **Git History Analysis:** Identify secrets introduced in previous commits.
- **Exposure Tracking:** Compare historical findings with the current repository snapshot to identify whether a secret is still present or has been removed from the current snapshot.

## 🧠 What Makes SecretGuard Different?

SecretGuard focuses on **explainable detection rather than simply flagging matching strings**.

It combines:

- Pattern matching to recognize known credential formats.
- Contextual analysis to understand where potential secrets appear.
- Shannon entropy to identify unpredictable values.
- Confidence scoring to explain the strength of detection.
- Git history analysis to uncover previously committed secrets.

This helps developers understand not only what was detected, but also why it was detected.

## 🛠️ Tech Stack

**Backend**
- Java 21
- Spring Boot
- Spring Web
- JGit
- Maven

**Detection Engine**
- Java Regular Expressions
- Contextual Analysis
- Shannon Entropy
- Confidence and Risk Scoring

**Frontend**
- React
- Vite
- CSS

**Testing**
- JUnit 5
- Mockito

**Deployment**
- Render (Backend)
- Vercel (Frontend)

## 🏗️ Project Structure

```text
SecretGuard/
│
├── secretGuard/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/java/
│   │   │   └── com/example/secretGuard/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── scanner/
│   │   │       ├── github/
│   │   │       ├── history/
│   │   │       ├── risk/
│   │   │       ├── validator/
│   │   │       └── config/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
└── secretguard-frontend/         # React frontend
    ├── src/
    ├── public/
    ├── package.json
    └── ...
```

## 🚀 Getting Started

### Prerequisites

- Java 21 or later
- Node.js and npm
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/RohiniShankari/secretGuard.git
cd secretGuard
```

### 2. Start the Backend

Open a terminal and navigate to the backend directory:

```bash
cd secretGuard
```

On Windows, run:

```cmd
.\mvnw.cmd spring-boot:run
```

The backend will run at:

`http://localhost:8080`

### 3. Configure the Frontend for Local Development

The deployed frontend uses the publicly deployed Render backend URL.

When running the frontend locally, you must change the API base URL in the frontend source code.

Find the API URL configuration or the file containing the backend `fetch()` request.

Replace the deployed Render backend URL with:

```javascript
http://localhost:8080
```

For example:

```javascript
const API_URL = "http://localhost:8080";
```

**Important:** Use the local URL only when running the frontend and backend on your own computer. The deployed Vercel application must continue using the deployed Render backend URL.

### 4. Start the Frontend

Open another terminal from the repository root:

```bash
cd secretguard-frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open the frontend at:

`http://localhost:5173`

### 5. Local Application URLs

| Component | URL |
|---|---|
| Local frontend | http://localhost:5173 |
| Local backend | http://localhost:8080 |
| Live frontend | https://secret-guard-5c3kck4g5-sankari.vercel.app/ |

Both the backend and frontend must be running locally for local scanning to work.

If CORS is enabled in the backend, make sure it allows `http://localhost:5173`.

## 🌐 Live Deployment

SecretGuard is deployed using:

- **Frontend:** Vercel
- **Backend:** Render

Visit the live application:

**[Open SecretGuard](https://secret-guard-5c3kck4g5-sankari.vercel.app/)**

The frontend communicates with the deployed Spring Boot backend to process repository scan requests.

Note: The backend may take some time to respond if the hosting service has placed it in an idle state.

## 🔌 API Endpoint

### Scan a GitHub Repository

**Method:** `POST`

**Endpoint:**

```text
/api/github-scan
```

**Request Body:**

```json
{
  "repositoryUrl": "https://github.com/owner/repository"
}
```

**Example Response:**

```json
[
  {
    "secretType": "AWS_ACCESS_KEY",
    "maskedValue": "AKIA********MPLE",
    "filePath": "config/aws.properties",
    "lineNumber": 2,
    "explanation": "Matches the format of an AWS access key ID.",
    "confidence": "HIGH",
    "confidenceScore": 85,
    "severity": "HIGH",
    "risk": "HIGH"
  }
]
```

*The response above is illustrative.*

## 🔒 Security Considerations

- Detected secret values are masked in scan results.
- Repository files are scanned without executing their code.
- Binary or unsupported files are skipped.
- Temporary cloned repositories are cleaned up after scanning.
- Confidence scores and risk classifications are heuristic indicators, not guarantees that a credential is valid or exploitable.
- Secret detection does not verify whether a credential is active.

**Important:** Removing a secret from the latest commit does not invalidate the credential or remove it from Git history. Exposed credentials should be rotated or revoked.

## 🚧 Future Improvements

- More credential patterns and provider-specific detection rules.
- Improved detection of binary files and complex repository structures.
- More comprehensive Git history and branch analysis.
- Exposure timeline visualization.
- Risk-based remediation recommendations.
- Secure scan-result persistence and reporting.
- Automated tests against labelled datasets to measure precision and recall.

## 👩‍💻 Author

**Rohini Sankari Byra**

- GitHub: [@RohiniShankari](https://github.com/RohiniShankari)

---

*SecretGuard — Detect secrets. Understand exposure. Secure your code.*etGuard — Detect secrets. Understand exposure. Secure your code.*
