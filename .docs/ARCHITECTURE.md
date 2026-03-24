# SomiQ — backend architecture

This document describes how the Ktor server is structured, why it is shaped that way, and the conventions to follow when adding features.

## Goals

- **Clear layers**: HTTP concerns stay in `server`, business rules in `domain`, persistence and integrations in `data`.
- **No framework types in domain**: The domain layer must not depend on Ktor types (e.g. avoid `io.ktor.server.plugins.NotFoundException` in use cases).
- **One place for HTTP error mapping**: Domain exceptions are turned into JSON + status codes in `Application.configureExceptionHandling()` via Ktor `StatusPages`.

## Package layout

| Package | Responsibility |
|--------|----------------|
| `com.ranjan.server` | Ktor `Application` extensions, routes, controllers (HTTP adapters), `ApplicationCall` helpers |
| `com.ranjan.domain` | Use cases, repository **interfaces**, domain models, **domain exceptions** |
| `com.ranjan.data` | Repository **implementations**, Exposed tables, DB, JWT/password adapters wired to domain interfaces |

Dependency direction: **`server` → `domain` ← `data`**. Domain does not import Ktor or Exposed.

## Request flow

1. **Routes** (`*Routes.kt`) — register paths, `authenticate(JwtConfig.NAME)` where needed, delegate to a controller method.
2. **Controllers** (`*Controller.kt`) — parse `ApplicationCall` (query, body, path), call use cases, map `Result` to responses.
3. **Use cases** — orchestrate one user action; depend on repository interfaces; throw or return `Result` with **domain** failures inside.
4. **Repositories** — implement persistence; may throw domain exceptions (e.g. `ResourceNotFoundException`) when an entity is missing.

## Dependency injection (Koin)

- **`dataModule`** — `Database`, repository implementations, `TokenProvider`, `PasswordCipher`, etc.
- **`domainModule`** — use cases (typically `factory`).
- **`appModule`** — controllers (typically `single`).

Load order in `Application.configureKoin()`: `dataModule`, `domainModule`, `appModule`.

## Authentication

- **JWT** is configured in `Application.configureSecurity()` using `JwtConfig` (secret/issuer/audience live in `data` for now).
- **Protected routes** use `authenticate(JwtConfig.NAME) { ... }` in route files.
- **Controllers** read the current user with `ApplicationCall.userId()` / `userIdOrNull()` (see `server/common/extension/ApplicationCallExtension.kt`).
- **Convention**: Prefer declaring auth on the **route** for endpoints that always require a token. For mixed public vs authenticated flows (e.g. paginated feeds), the controller may use `userIdOrNull()` or throw `UnauthorizedException` when a page requires auth.

### Device-bound token policy

- OTP verify request carries `deviceId` from client.
- Access, refresh, and signup JWTs include `deviceId` claim.
- `refresh_tokens` table stores `device_id` for each refresh token.
- Refresh flow validates **userId + token + deviceId** against DB before issuing new access token.
- If DB row is missing or device mismatch occurs, refresh fails with **401 Unauthorized**.

## Domain exceptions (single source of truth)

All defined in `com.ranjan.domain.exception` (`Exceptions.kt`):

| Exception | Typical HTTP mapping |
|-----------|----------------------|
| `InvalidUserIdException` | 400 |
| `UnauthorizedException` | 401 |
| `ValidationException` | 400 |
| `ResourceNotFoundException` | 404 |
| `ForbiddenException` | 403 |

`Application.configureExceptionHandling()` registers these so responses use `ErrorResponse(message)` JSON where applicable.

### Controllers and `Result`

Many use cases return `Result<T>` and capture failures with `runCatching`. When the failure is a **domain** exception above, controllers **rethrow** it so `StatusPages` produces a consistent JSON body:

```kotlin
result.onFailure { ex ->
    when (ex) {
        is ResourceNotFoundException, is ForbiddenException -> throw ex
        else -> call.respond(HttpStatusCode.InternalServerError, ErrorResponse("..."))
    }
}
```

Avoid duplicating `HttpStatusCode.NotFound` / `Forbidden` in every controller for the same exception types.

## Adding a new feature

1. Define or extend **domain models** and a **repository interface** in `domain`.
2. Implement the repository in **data** (Exposed + `dbQuery`).
3. Add **use cases** and register them in `domainModule`.
4. Add **controller** methods + **routes**; register the controller in `appModule`.
5. If you need a new HTTP error class, add a **domain exception** and map it in `configureExceptionHandling()`.

## Optional future improvements

- Split Gradle modules (`:domain`, `:data`, `:server`) to enforce compile-time boundaries.
- Move JWT secret/issuer to environment/config only (no hardcoded secrets).
- Restrict CORS in production (`anyHost()` is a placeholder).
- Align `userId()` failures with `UnauthorizedException` instead of `IllegalStateException` for a single auth error path.

## Changelog (architecture)

- **Domain exceptions consolidated** into `com.ranjan.domain.exception` (removed duplicate `domain/common/exceptions`).
- **Ktor `NotFoundException` removed** from domain and data layers; replaced with `ResourceNotFoundException`.
- **Centralized HTTP mapping** for `ResourceNotFoundException`, `ForbiddenException`, and `ValidationException` in `StatusPages`.
- **Fixed** post update/delete controller branches that referenced `AccessDeniedException` (use cases emit `ForbiddenException`).
- **Added device-bound refresh security**: persisted `device_id` in `refresh_tokens` and enforced match during token refresh.
