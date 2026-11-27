# Week 15 — Executed Security Test Matrix (CO8/CO9)

**Redaction Policy:** Session cookie values (`JSESSIONID`), CSRF token strings, and plaintext passwords are redacted (`<REDACTED>`) in logs and screenshots.

## Test Execution Summary

| # | Scenario | Identity / Role | CSRF Context | Request | Expected Status | Actual Status & DB/UI Result | Evidence |
| :- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ST-01** | Anonymous protected read denied | Anonymous (no cookie) | N/A (`GET`) | `GET /api/pets` | `401 Unauthorized` | `401 (Unauthorized)` in Console; UI displays Sign-In form | `evidence/01_anon_401.png` |
| **ST-02** | Valid login succeeds | `coordinator_mgr` (`ROLE_MANAGER`) | Fresh `X-CSRF-TOKEN` from `GET /api/csrf` | `POST /api/auth/login` | `200 OK` | `200` OK; `Signed in as coordinator_mgr (ROLE_MANAGER)` banner & full CRUD forms shown | `evidence/02_login_success.png` |
| **ST-03** | Invalid password denied | `coordinator_mgr` (wrong password) | Valid `X-CSRF-TOKEN` | `POST /api/auth/login` | `401 Unauthorized` | `Sign-in failed (401): Invalid username or password.` banner; no session created | `evidence/03_invalid_login_401.png` |
| **ST-04** | Permitted role reads | `volunteer_reader` (`ROLE_READER`) | Authenticated session | `GET /api/pets`, `GET /api/owners` | `200 OK` | `200` OK; read-only notice active, mutation forms hidden, Actions set to `Read-only` | `evidence/04_reader_get_200.png` |
| **ST-05** | Read-only role cannot mutate (with valid CSRF) | `volunteer_reader` (`ROLE_READER`) | Valid session + valid `X-CSRF-TOKEN` | `POST /api/pets` (`CHIP-99`) via Console `fetch()` | `403 Forbidden` | `403` error on `/api/pets`; MySQL `pets` table unchanged (still 4 rows) | `evidence/05_reader_post_403.png` |
| **ST-06** | Authorized manager can mutate | `coordinator_mgr` (`ROLE_MANAGER`) | Valid session + valid `X-CSRF-TOKEN` | `POST /api/pets` (`CHIP-1004`, Charlie) | `201 Created` | `Pet #4 (Charlie) created successfully.` banner & row `#4` committed in MySQL | `evidence/06_manager_mutate_201.png` |
| **ST-07** | Missing/invalid CSRF mutation denied | `coordinator_mgr` (`ROLE_MANAGER`) | Valid session, **omitted** `X-CSRF-TOKEN` | `POST /api/pets` (`CHIP-88`) via Console `fetch()` | `403 Forbidden` | `POST http://localhost:8080/api/pets 403 (Forbidden)`; MySQL `pets` unchanged | `evidence/07_missing_csrf_403.png` |
| **ST-08** | Logout invalidates session | `coordinator_mgr` -> logged out | Valid `X-CSRF-TOKEN` on logout, then `GET /api/pets` | `POST /api/auth/logout` then `GET /api/pets` | `200 OK` then `401 Unauthorized` | `Signed out.` banner; subsequent `GET /api/pets` returns `401 (Unauthorized)` | `evidence/08_logout_401.png` |

## Console / PowerShell Verification Commands

```javascript
// ST-01 & ST-08: Verify unauthenticated or logged-out read returns 401
fetch('/api/pets')

// ST-05: Signed in as volunteer_reader with valid CSRF token -> 403 Role Forbidden
fetch('/api/csrf')
  .then(r => r.json())
  .then(c => fetch('/api/pets', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', [c.headerName]: c.token },
    body: JSON.stringify({ microchipNumber: 'CHIP-99', name: 'Test', species: 'Dog' })
  }))

// ST-07: Signed in as coordinator_mgr without X-CSRF-TOKEN -> 403 CSRF Forbidden
fetch('/api/pets', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ microchipNumber: 'CHIP-88', name: 'NoCsrf', species: 'Cat' })
})