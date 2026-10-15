# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start Vite dev server (http://localhost:5173)
npm run build    # Type-check + production build
npm run lint     # ESLint
npm run preview  # Preview production build locally
npm run test:run # Vitest (headless)
```

The app always talks to the real backend (via the Vite `/api` proxy in dev). There is no in-browser mock backend.

## Local API Proxy

Vite proxies `/api/*` to `http://localhost:8080` — no `VITE_API_BASE_URL` needed. Override the backend target with `VITE_API_PROXY_TARGET` in a `.env.local` file if your backend runs on a different port.

## Architecture

**Auth state** lives in `src/auth/AuthContext.tsx`. Access tokens are stored in `sessionStorage`; the refresh token is an HTTP-only cookie. On bootstrap, `AuthContext` checks for a stored token or calls `/api/auth/refresh`. Auth status is one of `"bootstrapping" | "authenticated" | "anonymous"` — `App.tsx` gates rendering on this.

**API layer:**

- `src/api/client.ts` — central `apiRequest<T>()` function. Automatically attaches `Authorization: Bearer` header and retries once on 401 using the refresh token before calling `onAuthFailure`.
- `src/api/auth.ts` — login, logout, refresh, getCurrentUser
- `src/services/` — per-entity modules (`appointmentService`, `patientService`, etc.) that call `apiRequest`
- `src/queries/` — TanStack Query hooks over the services; pages use these, never the services directly.
  - `keys.ts` holds every query key. Reads: `useAppointments(date)`, `useTherapists()`, `usePatients()`, `usePatientSearch(q)` (debounced).
  - Writes are `useMutation` hooks (create/update/delete per entity, incl. `useUpdatePatient`) that invalidate what they change. Check-in (`useUpdateAppointmentStatus`) is optimistic and rolls back on error.
  - `useAppointments` keeps the previous date as placeholder data (`isPlaceholderData`) while a new date loads.
  - `queryClient.ts`: 30s stale time; queries retry once on network/5xx errors, never on 4xx; writes don't retry. `App.tsx` clears the cache on logout so another clinic never sees it.

**Data model (flat):** `Patient {firstName,lastName}`, `Therapist {name}`, `Appointment {patient, therapist, scheduledAt, type, status}`. `type` = Evaluation/Reassessment/Follow-up. `status` is a minimal 3-state check-in lifecycle: `SCHEDULED → WAITING → DONE`. There is no Case or Body Region. Types live one-per-entity in `src/types/` (`patient.ts`, `therapist.ts`, `appointment.ts`, `common.ts`). The API returns denormalized `BoardRow`s (patient/therapist names inline) for both surfaces.

**Two surfaces / role-based routing** (`src/App.tsx` renders by `currentUser.roleName`):

- **ADMIN (front desk):** `ConsoleLayout` (`src/features/console/`) — `/` = `SchedulePage` (today's schedule + fast-add row with patient typeahead + Arrived/Start check-in), `/patients`, `/therapists`; `/board` opens the clinic display.
- **DISPLAY:** `/` = `ScheduleDisplayPage` only — read-only TV board, auto-refreshing every 30s, **Currently Waiting** large + **Up Next** smaller. It's a public screen, so patients show as first name + last initial (`boardName()` in `src/types/patient.ts`); the console keeps full names. It has a therapist color legend, a live "Updated Xs ago" indicator (amber after 2 min without a successful refresh), and a dark mode button. Type sizes scale with screen width for 1080p and 4K TVs.

**Testing:** the Vitest suite mocks the API with MSW in `src/test/msw/` (node server, wired up in `src/test/setup.ts`). This is test-only — the running app has no mock backend and always hits the real API.

## UI Stack

- **MUI v7** — use for all UI components (layout, data grids, dialogs, chips, etc.). Style with the `sx` prop and theme overrides in `src/theme.ts`; there is no Tailwind or other CSS framework.
- **Theme (`src/theme.ts`)** — the single source of colors. It defines light (default) and dark color schemes as CSS variables (`colorSchemeSelector: "data"` → `data-light` / `data-dark` on `<html>`). Never hard-code colors in components:
  - Use palette paths in `sx` (`"text.secondary"`, `"background.subtle"`, `"status.waiting"`), or `theme.vars.palette.*` in an `sx` callback. `theme.palette.*` holds light-mode values only, so it won't follow dark mode.
  - For transparency use `theme.alpha(theme.vars.palette.primary.main, 0.1)`, not the standalone `alpha()` helper.
  - Custom tokens: `background.subtle`, `border.strong`, `elevation.{card,raised,overlay}` (box shadows), `status.{scheduled,waiting,done}`, `therapist[]` (board accents).
  - The color mode is saved in `localStorage` (`mui-mode`); `ColorModeToggle` switches it, and an inline script in `index.html` applies it before first paint.
- **MUI X DataGrid** — used in `PatientsDirectory` only (search filters the loaded rows client-side). `SchedulePage` is an agenda list grouped by time, and `TherapistsManage` is a card grid.

## Key Conventions

- Server data lives in TanStack Query (`src/queries/`); don't fetch in `useEffect`. React Context is for app state (auth, toasts), local `useState` for component state.
- All API calls go through `apiRequest` from `src/api/client.ts` — never use `fetch` directly in components or services.
- Front-desk pages live under `src/features/console/`.
- Shared UI components live in `src/components/ui/`. Build pages from them instead of hand-rolling the pattern:
  - `PageHeader` (with optional `onRefresh`), `SectionCard` (titled card; `disablePadding` for tables/lists), `EmptyState`, `ListSkeleton` for first load, `StatCard` for summary numbers (optionally a toggle).
  - `StatusChip` for appointment status; `TherapistBadge` + `useTherapistAccents()` for therapist colors (assigned by position in the full therapist list, so they match across console and board).
  - `ConfirmDialog` (`tone="danger"`, pass `loading` while the request runs).
  - `FormDrawer` for adding and editing records (side drawer, full width on phones); used by Schedule, Patients and Therapists.
  - Toasts: call `useToast()` (`toast.success(...)`, `toast.error(...)`); `ToastProvider` is mounted once in `main.tsx`. Don't add per-page `Snackbar`s.
- Types are co-located one-per-entity in `src/types/`.
