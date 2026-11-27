# Week 15 (CO8/CO9) — Authentication, Role Authorization & Session/CSRF Security

## 1. Authentication vs. Authorization
* **Authentication (Who are you?):** Verifies user identity via `CustomUserDetailsService` and `BCryptPasswordEncoder` during `POST /api/auth/login`, binding the authenticated `Principal` to a server-side `HttpSession`.
* **Authorization (What are you allowed to do?):** Enforces role rules (`ROLE_READER` vs. `ROLE_MANAGER`) at the HTTP filter boundary (`SecurityFilterChain`) and record-level ownership rules inside `OwnerService`. Hiding forms in Vanilla JS improves UX, while backend rules reject unauthorized direct `fetch()`/`curl` calls independently.

## 2. Permission & Ownership Matrix

| Role | Target Persona | Allowed Endpoints / Actions | Denied Actions (`403 Forbidden`) | Record Visibility & Ownership |
| :--- | :--- | :--- | :--- | :--- |
| **Anonymous** | Unauthenticated visitor | `GET /`, static assets, `GET /api/csrf`, `POST /api/auth/login` | All `/api/pets/**`, `/api/owners/**`, `/api/adoptions/**` (`401 Unauthorized`) | No access to domain records |
| **`ROLE_READER`** | Shelter Volunteer | `GET /api/pets/**`, `GET /api/owners/**`, `GET /api/adoptions/**`, `GET /api/auth/me`, `POST /api/auth/logout` | `POST`, `PUT`, `DELETE` on `/api/pets`, `/api/owners`, `/api/adoptions` | Read-only UI (`Read-only` actions, mutation forms hidden); can inspect registry tables |
| **`ROLE_MANAGER`** | Adoption Coordinator | All `ROLE_READER` endpoints + `POST`/`PUT`/`DELETE` on `/api/pets`, `/api/owners`, and `POST /api/adoptions` | Deleting an `Owner` created by another coordinator (`403` ownership check) | Full CRUD forms and actions; `created_by` stamped on `Owner` creation |

## 3. Session-Based Design vs. Token Choice
* **Choice:** Server-side `HttpSession` authentication (`JSESSIONID`) with same-origin Vanilla JS served from Spring Boot (`http://localhost:8080`).
* **Cookie & Session Lifecycle:**
  * `HttpOnly=true` prevents JavaScript (`document.cookie`) from reading the session ID.
  * `SameSite=Lax` mitigates cross-site cookie attachment on top-level navigation.
  * `Secure` is configurable via `COOKIE_SECURE` (`true` under HTTPS deployment).
  * Login invalidates any pre-existing session to prevent session fixation; `POST /api/auth/logout` invalidates the `HttpSession` and clears `SecurityContextHolder`.
  * No passwords or session tokens are stored in `localStorage` or `sessionStorage`.

## 4. Password Storage & Safe Provisioning
* Passwords are hashed using `BCryptPasswordEncoder(12)` before persistence in `app_users.password_hash`.
* DTOs and `/api/auth/me` return only `{ username, role }`—never password hashes.
* Public self-registration with client-supplied roles is disabled; test accounts are provisioned locally via environment variables (`APP_READER_PASSWORD`, `APP_MANAGER_PASSWORD`).

## 5. CSRF vs. CORS & Token Lifecycle
* **CORS** governs which origins browsers allow to read cross-origin responses; it does **not** authenticate callers or stop cross-site form/fetch forgery when cookies are sent automatically.
* **CSRF Protection** remains enabled (`HttpSessionCsrfTokenRepository`). The frontend calls `GET /api/csrf`, stores `{ headerName, token }` in JavaScript memory, and sends `X-CSRF-TOKEN` on every `POST`, `PUT`, and `DELETE` request.
* A fresh CSRF token is fetched immediately after login and logout because authentication state changes rotate the session/CSRF context.

## 6. Annotated `SecurityConfig` Summary
* **Public Matchers:** `/`, `/index.html`, `/style.css`, `/app.js`, `GET /api/csrf`, and `POST /api/auth/login`.
* **Role Matchers:** `HttpMethod.GET` on `/api/pets/**`, `/api/owners/**`, `/api/adoptions/**` requires `hasAnyRole("READER", "MANAGER")`; mutating methods (`POST`, `PUT`, `DELETE`) require `hasRole("MANAGER")`.
* **API Error Handlers:** Custom `AuthenticationEntryPoint` (`401` JSON) and `AccessDeniedHandler` (`403` JSON) prevent Spring Security from redirecting `fetch()` calls to an HTML login page.