# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start Vite dev server (http://localhost:5173)
npm run build    # Type-check + production build
npm run lint     # ESLint
npm run preview  # Preview production build locally
```

## Local API Proxy

Vite proxies `/api/*` to `http://localhost:8080` — no `VITE_API_BASE_URL` needed. Override the backend target with `VITE_API_PROXY_TARGET` in a `.env.local` file if your backend runs on a different port.

## Architecture

**Auth state** lives in `src/auth/AuthContext.tsx`. Access tokens are stored in `sessionStorage`; the refresh token is an HTTP-only cookie. On bootstrap, `AuthContext` checks for a stored token or calls `/api/auth/refresh`. Auth status is one of `"bootstrapping" | "authenticated" | "anonymous"` — `App.tsx` gates rendering on this.

**API layer:**
- `src/api/client.ts` — central `apiRequest<T>()` function. Automatically attaches `Authorization: Bearer` header and retries once on 401 using the refresh token before calling `onAuthFailure`.
- `src/api/auth.ts` — login, logout, refresh, getCurrentUser
- `src/services/` — per-entity modules (`appointmentService`, `patientService`, etc.) that call `apiRequest`

**Admin CRUD pattern:** All admin feature pages use `useEntityCrud` (`src/hooks/useEntityCrud.ts`), a generic hook that wraps list/create/update/delete and manages loading/error state. Pass a service object and list params; the hook exposes `rows`, `createEntity`, `updateEntity`, `deleteEntity`, `refresh`, and `mutationKind`.

**Routing:**
- `/` — `ScheduleShell` (schedule board, all authenticated users)
- `/admin/*` — `AdminLayout` (ADMIN role only, guarded by `RequireAdmin`)
  - `/admin/appointments`, `/admin/patients`, `/admin/therapists`, `/admin/body-regions`

## UI Stack

- **MUI v7** — use for all UI components (layout, data grids, dialogs, chips, etc.)
- **Tailwind v3** — available for utility classes where MUI's `sx` prop would be verbose
- **MUI X DataGrid** — used in all admin list pages; pagination is server-side

## Key Conventions

- No external state management library — use React Context for shared state, local `useState` for component state.
- All API calls go through `apiRequest` from `src/api/client.ts` — never use `fetch` directly in components or services.
- Feature pages live under `src/features/{domain}/` (e.g., `AppointmentsPage`, `AppointmentDialog`).
- Shared admin UI components (layout, styles, confirm dialog) live in `src/components/admin/`.
- Types are co-located in `src/types/`.
