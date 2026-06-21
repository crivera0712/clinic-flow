# ClinicFlow UI

React/Vite frontend for the ClinicFlow backend API.

## Environment

In local dev the app calls `/api`, which Vite proxies to the backend at
`http://localhost:8080` — no env config is required. Optional overrides (set them in a
`.env.local` file):

```bash
VITE_API_PROXY_TARGET=http://localhost:8080   # backend the dev proxy points at
VITE_CLINIC_NAME=Mill Valley Physical Therapy # clinic name shown on the display board
```

For a deployed frontend talking to a different API origin, set:

```bash
VITE_API_BASE_URL=https://clinic-flow-api.duckdns.org/api
```

## Commands

```bash
npm install
npm run dev        # Vite dev server (http://localhost:5173)
npm run build      # type-check + production build
npm run lint       # ESLint
npm run test:run   # Vitest (headless)
npm run preview    # preview the production build
```
