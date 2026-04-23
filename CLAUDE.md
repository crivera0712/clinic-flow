# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Layout

```
clinic-flow/
├── backend/          # Spring Boot API (Java 17)
└── client/
    └── clinicflow-ui/  # React 19 / Vite frontend
```

Each subdirectory has its own CLAUDE.md with stack-specific detail.

## What ClinicFlow Does

Multi-tenant scheduling backend for physical therapy clinics. Manages appointments, therapists, patients, and cases. Two user roles:

- `ADMIN` — full access to admin panel (CRUD on all entities) + schedule board
- `DISPLAY` — schedule board view only

A demo clinic (`demo_admin` / `demo_display`) is seeded and kept read-only; `DemoScheduler` reseeds it periodically.

## Authentication Flow

```
POST /api/auth/{clinicSlug}/login
  → creates auth_session row in DB
  → returns JWT access token (Bearer) + refresh token (HTTP-only cookie)

Every request:
  JwtAuthenticationFilter → JwtService.parseToken() → AuthSessionService.isAccessSessionActive()
  → sets AuthPrincipal(userId, username, sid, clinicId) in SecurityContext
```

Refresh uses the HTTP-only cookie (`POST /api/auth/refresh`). The frontend auto-retries any 401 with a refresh before failing.

## Multi-Tenancy

All operational tables have a `clinic_id` column. Services and repositories always scope queries by `clinicId` from the current `AuthPrincipal`. Never query without clinic scoping.

## Deployment

- Backend: Docker container on EC2, PostgreSQL on same instance, Caddy TLS at `clinic-flow-api.duckdns.org`
- Frontend: Render static site at `clinic-flow-4hei.onrender.com`
