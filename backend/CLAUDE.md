# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AppointmentControllerTest

# Run a single test method
./mvnw test -Dtest=AppointmentControllerTest#methodName

# Build without running tests
./mvnw package -DskipTests
```

## Environment Setup

Create `backend/.env` (loaded automatically via spring-dotenv):

```
DB_URL=jdbc:postgresql://localhost:5432/clinicflow
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-me-to-a-long-random-secret
```

Tests run against H2 in-memory — no database setup needed for `./mvnw test`.

## Package Structure

Each domain is a self-contained package under `com.clinicflow.clinic_flow`:

```
appointment/     cases/       patient/      therapist/
body_region/     clinics/     auth/         auth_sessions/
users/           demo/        config/       exception/
```

Each domain package follows: `Entity`, `Repository`, `Service`, `Controller`, `Mapper`, and a `dtos/` sub-package with `*RequestDto`, `*ResponseDto`, `*PatchDto`.

## Key Architectural Patterns

**DTO/Mapper pattern:** All API boundaries use DTOs. MapStruct mappers (`@Mapper(componentModel = "spring")`) handle entity ↔ DTO conversion. Never return raw entities from controllers.

**Clinic scoping (multi-tenancy):** Every service method receives `clinicId` from `AuthPrincipal` (injected via `@AuthenticationPrincipal`). All repository methods include clinic-scoped variants like `findByIdAndClinicId(...)`. Always add clinic scoping when writing new repository methods.

**Auth principal:** `AuthPrincipal` is a record carrying `userId`, `username`, `sid` (session ID), and `clinicId`. Available in any `@PreAuthorize`-protected endpoint via `@AuthenticationPrincipal AuthPrincipal principal`.

**Exception handling:** Domain-specific exceptions (e.g., `AppointmentNotFoundException`, `DemoClinicReadOnlyException`) are defined in `exception/` and mapped to HTTP responses in `GlobalExceptionHandler`.

**Pagination:** Controllers return `PageResponse<T>` (a common wrapper in `common/`) for paginated endpoints.

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration/` following `V{n}__{description}.sql`. New migrations must increment the version number. The demo clinic is seeded in `V23__seed_demo_clinic.sql` and expanded in `V24__expand_demo_clinic_seed.sql`.

## Testing Patterns

| Layer | Annotation | Database |
|-------|-----------|----------|
| Controller | `@WebMvcTest` + `MockMvc` | Mocked (Mockito) |
| Repository | `@DataJpaTest` | H2 in-memory |
| Service | `@ExtendWith(MockitoExtension.class)` | Mocked (Mockito) |

Controller tests import `GlobalExceptionHandler` via `@Import` to test error responses. Use `@MockitoBean` (Spring 6.2+) for dependency mocking in `@WebMvcTest`.

## Security Config

`SecurityConfig` permits `/api/auth/**` without authentication. All other `/api/**` routes require a valid JWT with an active session. Role-based access uses `ROLE_ADMIN` and `ROLE_DISPLAY` Spring Security authorities.
