# SecretGuard 🔐

**An Explainable GitHub Repository Secret Scanner**

SecretGuard is a Java Spring Boot-based security tool that scans GitHub repositories to identify potentially exposed secrets such as API keys, access tokens, passwords, and private keys.

Unlike basic pattern-matching scanners, SecretGuard combines multiple detection techniques to improve detection quality, explain why a value was flagged, and identify secrets that may have been exposed in previous Git commits.

## 🚨 Problem

Developers sometimes accidentally commit sensitive information such as API keys, passwords, and access tokens into Git repositories.

Even when these secrets are removed from the latest version of a project, they may still exist in Git history. Manually identifying these exposures is time-consuming, and simple pattern-based detection can generate false positives.

## 💡 Solution

SecretGuard scans GitHub repositories using a combination of pattern matching, contextual analysis, and entropy-based detection.

It provides masked findings with explanations, confidence scores, and risk classifications. It also analyzes Git commit history to identify secrets that were introduced in earlier commits, even if they are no longer present in the current files.

## ✨ Key Features

- **GitHub Repository Scanning:** Scan public GitHub repositories using their repository URLs.
- **Pattern-Based Detection:** Detect common secret formats, including AWS access keys, GitHub tokens, and private key headers.
- **Contextual Analysis:** Analyze sensitive keywords around potential secrets to improve detection.
- **Entropy Analysis:** Identify potentially sensitive values based on randomness and unpredictability.
- **Placeholder Detection:** Suppress common dummy values and placeholder credentials.
- **Confidence Scoring:** Assign confidence scores using multiple detection signals.
- **Risk Classification:** Categorize findings based on severity and confidence.
- **Secret Masking:** Mask detected values to avoid exposing credentials in scan results.
- **Git History Scanning:** Identify secrets introduced in previous commits, even when removed from the latest snapshot.
- **Exposure Tracking:** Identify whether a historical secret is still present or has been removed from the current repository snapshot.

## 🧠 What Makes SecretGuard Unique?

SecretGuard focuses on **explainable detection rather than simply flagging matching strings**.

It combines:
- Pattern matching to recognize known credential formats.
- Contextual analysis to understand where potential secrets appear.
- Shannon entropy to identify unpredictable values.
- Confidence scoring to explain the strength of detection.
- Git history analysis to uncover previously committed secrets.

This combination helps developers understand not only what was detected, but also why it was detected and whether it remains in the current repository.

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

## 🏗️ Project Structure

```text
secretGuard/
│
├── secretGuard/              # Spring Boot backend
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
│   └── pom.xml
│
└── secretguard-frontend/      # React frontend
```

## ⚙️ Getting Started

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

Navigate to the backend directory:

```bash
cd secretGuard
```

Run the Spring Boot application on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend will run at:

`http://localhost:8080`

### 3. Start the Frontend

Open another terminal and navigate to the frontend directory:

```bash
cd secretGuard/secretguard-frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open:

`http://localhost:5173`

## 🔌 API Endpoint

### Scan a GitHub Repository

**Endpoint:** `POST /api/github-scan`

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

**Note:** Removing a secret from the latest commit does not invalidate the credential or remove it from Git history. Exposed credentials should be rotated or revoked.

## 🚀 Future Improvements

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

*SecretGuard — Detect secrets. Understand exposure. Secure your code.*
