# SomiQ Server

Backend for **SomiQ**, a social app with posts, reels, stories, comments, collections, notifications and search. Built with Ktor and deployed to Zoho Catalyst AppSail.

## Technology stack

- **Framework:** Ktor 3 (Netty)
- **Language:** Kotlin
- **Database:** PostgreSQL in production, H2 for local development
- **Database library:** Exposed with HikariCP
- **Authentication:** phone OTP, then JWT access and refresh tokens
- **Media storage:** Zoho Catalyst Stratus (default), with Google Cloud Storage and local disk as alternatives
- **Dependency injection:** Koin
- **Build:** Gradle, packaged as a fat JAR in a Docker image

## Project structure

```
core/   Shared infrastructure: config, database, storage, Ktor plugins, utilities
chat/   Chat feature module (in progress)
src/    Main application
  server/   Routes and controllers
  domain/   Use cases, repository interfaces and models
  data/     Repository implementations and database tables
```

See [.docs/ARCHITECTURE.md](.docs/ARCHITECTURE.md) for more detail.

## Configuration

All settings live in [`src/main/resources/application.yaml`](src/main/resources/application.yaml). Values that are secret or differ per environment are read from environment variables:

- `"$VAR"` is required. The server will not start without it.
- `"$VAR:default"` is optional and falls back to the default.

Non-secret settings, such as the JWT issuer, audiences and token lifetimes, are set directly in `application.yaml`.

### Environment variables

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | Secret used to sign tokens. At least 32 characters. |
| `DB_URL` | Yes | JDBC URL. `jdbc:postgresql:...` or `jdbc:h2:...`. The driver is picked from the URL. |
| `DB_USER` | Yes | Database user. |
| `DB_PASSWORD` | No | Database password. Empty by default. |
| `APP_ENV` | No | `LOCAL` (default) or `PRODUCTION`. In production, CORS only allows `ALLOWED_CORS_HOSTS`. |
| `ALLOWED_CORS_HOSTS` | No | Comma-separated hosts allowed by CORS in production. |
| `PORT` | No | Port to listen on. Default `8080`. |
| `STORAGE_PROVIDER` | No | `STRATUS` (default), `GCS` or `LOCAL`. |
| `STRATUS_BUCKET_URL` | With `STRATUS` | Bucket URL, for example `https://somiq-uploads-development.zohostratus.in`. |
| `ZOHO_CLIENT_ID` | With `STRATUS` | Zoho Self Client ID. |
| `ZOHO_CLIENT_SECRET` | With `STRATUS` | Zoho Self Client secret. |
| `ZOHO_REFRESH_TOKEN` | With `STRATUS` | Zoho OAuth refresh token with Stratus scopes. |
| `GCS_BUCKET_NAME` | With `GCS` | Google Cloud Storage bucket. |
| `GCS_CREDENTIALS_JSON` | With `GCS` | Service account key JSON (or use `GCS_CREDENTIALS_PATH`). |

[`.env.example`](.env.example) lists every variable with sample values.

## Local development

1. **Set environment variables.** Ktor reads real environment variables only, not `.env` files. Copy `.env.example` to `.env.local`, fill it in, and load it with the IntelliJ EnvFile plugin, or add the variables to your run configuration. For a quick local setup:

    ```
    JWT_SECRET=local-development-secret-key-minimum-32-bytes-long!
    DB_URL=jdbc:h2:file:./build/db;DB_CLOSE_DELAY=-1
    DB_USER=root
    STORAGE_PROVIDER=LOCAL
    ```

    With `STORAGE_PROVIDER=LOCAL`, uploads are saved to `./uploads` and served from `/uploads`, so no Zoho credentials are needed.

2. **Run the server:**

    ```bash
    ./gradlew run
    ```

    The API is available at `http://localhost:8080`. Check it with `GET /health`.

3. **Run the tests:**

    ```bash
    ./gradlew test
    ```

## Authentication flow

1. `POST /auth/verify-otp` with `phone`, `otp` and `deviceId`. The OTP is currently a fixed development code, `000000`.
    - Existing user: the response has status `LOGGED_IN`, with access and refresh tokens.
    - New user: the response has status `SIGNUP_REQUIRED`, with a short-lived `signupToken`.
2. New users call `POST /auth/complete-signup` with `Authorization: Bearer <signupToken>` and their name and user ID. Use `POST /auth/check-user-id` first to check the user ID is free.
3. Send the access token as `Authorization: Bearer <accessToken>` on protected endpoints.
4. When the access token expires, call `POST /auth/refresh` with the refresh token.

## API overview

All endpoints except `/auth/*` and `/health` are under `/v1`. "Auth" means a bearer access token is required.

| Area | Endpoints | Auth |
|---|---|---|
| System | `GET /`, `GET /health`, `GET /checkUpdate` | No |
| Auth | `POST /auth/verify-otp`, `/auth/check-user-id`, `/auth/complete-signup`, `/auth/refresh` | No |
| Auth | `POST /auth/logout` | Yes |
| Media | `POST /v1/media/upload` (multipart, optional `?type=profile_pic`) | Yes |
| Posts | `GET/POST /v1/posts`, `GET /v1/posts/bookmarks`, `GET/PUT/DELETE /v1/posts/{id}`, `POST /v1/posts/{id}/like`, `POST /v1/posts/{id}/bookmark` | Yes |
| Reels | `GET /v1/reels`, `GET /v1/reels/{id}` | No |
| Reels | `POST /v1/reels`, `PUT/DELETE /v1/reels/{id}`, `POST /v1/reels/{id}/like`, `POST /v1/reels/{id}/bookmark` | Yes |
| Comments | `GET /v1/comments?postId=` or `?reelId=` (optional `parentCommentId`) | No |
| Comments | `POST /v1/comments?postId=` or `?reelId=`, `PUT/DELETE /v1/comments/{commentId}`, `POST /v1/comments/{commentId}/like` | Yes |
| Stories | `GET/POST /v1/stories`, `GET /v1/stories/me`, `GET /v1/stories/user/{userId}`, `GET/DELETE /v1/stories/{storyId}`, `POST /v1/stories/{storyId}/view` | Yes |
| Notifications | `GET /v1/notifications`, `GET /v1/notifications/unread-count`, `PUT /v1/notifications/{notificationId}/read`, `PUT /v1/notifications/read-all` | Yes |
| Collections | `GET/POST /v1/collections`, `PUT/DELETE /v1/collections/{collectionId}`, `GET/POST /v1/collections/{collectionId}/items`, `DELETE /v1/collections/{collectionId}/items/{itemId}` | Yes |
| Search | `GET /v1/search?q=&type=&limit=` (`type`: `all`, `users`, `posts` or `reels`) | No |
| Account | `GET /v1/account/{userId}` | No |
| Account | `GET /v1/account`, `PUT /v1/account/profile`, `POST /v1/account/follow/{userId}` | Yes |

List endpoints use cursor pagination with `limit` (default 20) and `after` (the cursor from the previous page).

### Postman

Import [`SomiQ_API.postman_collection.json`](SomiQ_API.postman_collection.json). Set `base_url` if you are not running locally, then run **Authentication > Verify OTP**. The returned tokens are saved to the collection variables, and every other request uses them automatically.

## Deployment

Pushing to `main` runs the GitHub Actions workflow in [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml). It:

1. Builds the fat JAR with `./gradlew buildFatJar`.
2. Builds the Docker image from the [`Dockerfile`](Dockerfile).
3. Deploys it to Zoho Catalyst AppSail with the Catalyst CLI.

The workflow needs these GitHub secrets: `CATALYST_TOKEN`, `CATALYST_ORG`, `CATALYST_PROJECT` and `CATALYST_DC`.

Set the application's environment variables (see [Configuration](#configuration)) in the Catalyst console under AppSail > Environment Variables, for the environment you deploy to. Catalyst keeps Development and Production variables separately.

## Licence

This project is licensed under the MIT Licence.
