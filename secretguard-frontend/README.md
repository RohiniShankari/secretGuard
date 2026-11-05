# SecretGuard Frontend

A React + Vite dashboard for the SecretGuard Spring Boot backend.

## Requirements
- Node.js 18.18+ (Node 20 LTS recommended)
- SecretGuard Spring Boot backend running (default: `http://localhost:8080`)

## Run locally
1. Extract this ZIP.
2. Open a terminal in the extracted `secretguard-frontend` folder.
3. Run:
   ```bash
   npm install
   npm run dev
   ```
4. Open the local URL printed by Vite (normally `http://localhost:5173`).

## Backend connection
The UI defaults to `http://localhost:8080`. You can change it in the app using
Settings (top-right) → Spring Boot API base URL.

Optional: copy `.env.example` to `.env` and edit `VITE_API_BASE_URL` before starting Vite.

## API endpoints expected
- `GET /api/scans` — scan history (expects an array)
- `POST /api/github-scan` — JSON body `{ "repositoryUrl": "https://github.com/owner/repository" }`
  and expects an array of findings with fields such as:
  `secretType`, `maskedValue`, `filePath`, `lineNumber`, `explanation`,
  `confidence`, `confidenceScore`, `severity`, `risk`.

If the API response shape differs, the scan screen will show an error rather than silently
pretending the scan succeeded.

## CORS
When the browser blocks requests from `http://localhost:5173`, configure Spring Boot CORS
to allow that origin for the required `/api/**` routes. Do not use `*` in production.

## Exposure timeline
The UI includes the timeline page and explains the current integration status. The backend
service currently has timeline logic, but the REST endpoint to fetch timeline events has not
been connected in this frontend yet. This page deliberately does not fabricate events.

## Production build
```bash
npm run build
```
The output is generated in `dist/`.
