# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start Vite dev server (http://localhost:5173)
npm run build    # Type-check + production build
npm run lint     # ESLint
npm run preview  # Preview production build locally
npm run test:run # Vitest (headless)

# Run the UI against the in-memory MSW mock backend (no real backend needed):
VITE_USE_MOCKS=true npm run dev   # boots authenticated as ADMIN; .env.local sets this for dev
```

## Local API Proxy

Vite proxies `/api/*` to `http://localhost:8080` — no `VITE_API_BASE_URL` needed. Override the backend target with `VITE_API_PROXY_TARGET` in a `.env.local` file if your backend runs on a different port.

## Architecture

**Auth state** lives in `src/auth/AuthContext.tsx`. Access tokens are stored in `sessionStorage`; the refresh token is an HTTP-only cookie. On bootstrap, `AuthContext` checks for a stored token or calls `/api/auth/refresh`. Auth status is one of `"bootstrapping" | "authenticated" | "anonymous"` — `App.tsx` gates rendering on this.

**API layer:**
- `src/api/client.ts` — central `apiRequest<T>()` function. Automatically attaches `Authorization: Bearer` header and retries once on 401 using the refresh token before calling `onAuthFailure`.
- `src/api/auth.ts` — login, logout, refresh, getCurrentUser
- `src/services/` — per-entity modules (`appointmentService`, `patientService`, etc.) that call `apiRequest`

**Data model (flat):** `Patient {firstName,lastName}`, `Therapist {name}`, `Appointment {patient, therapist, scheduledAt, type, status}`. `type` = Evaluation/Reassessment/Follow-up. `status` is a minimal 3-state check-in lifecycle: `SCHEDULED → WAITING → DONE`. There is no Case or Body Region. Types live one-per-entity in `src/types/` (`patient.ts`, `therapist.ts`, `appointment.ts`, `common.ts`). The API returns denormalized `BoardRow`s (patient/therapist names inline) for both surfaces.

**Two surfaces / role-based routing** (`src/App.tsx` renders by `currentUser.roleName`):
- **ADMIN (front desk):** `ConsoleLayout` (`src/features/console/`) — `/` = `SchedulePage` (today's schedule + fast-add row with patient typeahead + Arrived/Start check-in), `/patients`, `/therapists`; `/board` opens the clinic display.
- **DISPLAY:** `/` = `ScheduleDisplayPage` only — read-only, auto-refreshing, **Currently Waiting** large + **Up Next** smaller.

**Mock backend:** `src/mocks/` holds an in-memory MSW browser worker (`db.ts`, `handlers.ts`, `browser.ts`) started from `main.tsx` when `VITE_USE_MOCKS=true`. It implements the same API contract the real backend will (auth boots as ADMIN). Test-side MSW lives separately in `src/test/msw/`.

## UI Stack

- **MUI v7** — use for all UI components (layout, data grids, dialogs, chips, etc.)
- **Tailwind v3** — available for utility classes where MUI's `sx` prop would be verbose
- **MUI X DataGrid** — used in `PatientsDirectory` only. `SchedulePage` renders today's appointments as an MUI `Table`, and `TherapistsManage` uses a plain MUI list.

## Key Conventions

- No external state management library — use React Context for shared state, local `useState` for component state.
- All API calls go through `apiRequest` from `src/api/client.ts` — never use `fetch` directly in components or services.
- Front-desk pages live under `src/features/console/`.
- Shared UI components (styles, confirm dialog) live in `src/components/admin/` (`adminStyles.ts`, `ConfirmDeleteDialog.tsx`).
- Types are co-located one-per-entity in `src/types/`.
