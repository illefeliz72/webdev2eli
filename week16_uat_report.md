# Week 16 — Feature-Complete Application (F4) UAT & Test Report

**Project:** Pet Adoption Management System (`com.elifelize.webdev2eli`)  
**Stack:** Spring Boot 3.2.0, Spring Security, Spring Data JPA, MySQL 8 (`pet_adoption_test_db`), JUnit 5, Mockito, MockMvc  
**Branch:** `week16`

---

## 1. Automated Test Execution Summary

All three automated test suites executed against the isolated `test` profile (`pet_adoption_test_db` with `ddl-auto=create-drop`) via `.\mvnw.cmd test`:

| Test Suite | Layer & Focus | Tests Run | Passed | Failed |
| :--- | :--- | :---: | :---: | :---: |
| `AdoptionAndOwnerServiceTest` | Isolated business rules (double-adoption guard, missing pet check, owner-with-pets delete guard) | 3 | 3 | 0 |
| `SecurityAndApiIntegrationTest` | Spring Security RBAC (`401`/`403`), CSRF enforcement, DTO validation (`400`), missing ID (`404`), duplicate conflict (`409`) | 5 | 5 | 0 |
| `AdoptionTransactionMySqlTest` | Real MySQL `@Transactional` atomic commit and post-write failure rollback verified via `JdbcTemplate` | 2 | 2 | 0 |
| **Total** | **All Automated Suites (`BUILD SUCCESS`)** | **10** | **10** | **0** |

---

## 2. End-to-End UAT Test Matrix

| ID | Scenario | Actor / Role | Action Taken | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| UAT-01 | Unauthenticated API access | Anonymous | `GET /api/pets` without session | `401 Unauthorized` JSON response | Returned `401 Unauthorized` JSON | Pass |
| UAT-02 | Read-only role view | `volunteer_reader` (`ROLE_READER`) | Login and view pets, owners, and adoptions | `200 OK`; read lists render cleanly | Lists loaded with `200 OK` | Pass |
| UAT-03 | Read-only role mutation block | `volunteer_reader` (`ROLE_READER`) | Attempt `POST /api/pets` with valid CSRF token | `403 Forbidden`; no database write | Blocked with `403 Forbidden` | Pass |
| UAT-04 | Create Owner & Pet | `coordinator_mgr` (`ROLE_MANAGER`) | Submit valid Owner and Pet forms with CSRF | `201 Created`; records persist on reload | Created (`201`) and persisted | Pass |
| UAT-05 | Input validation feedback | `coordinator_mgr` (`ROLE_MANAGER`) | Submit blank required fields on `POST /api/pets` | `400 Bad Request` with field error details | Returned `400` validation payload | Pass |
| UAT-06 | Duplicate microchip constraint | `coordinator_mgr` (`ROLE_MANAGER`) | Submit duplicate `microchipNumber` on `POST /api/pets` | `409 Conflict` (no raw `500` stack trace) | Returned clean `409 Conflict` | Pass |
| UAT-07 | Atomic Pet Adoption workflow | `coordinator_mgr` (`ROLE_MANAGER`) | Adopt an `AVAILABLE` pet for a registered owner | Pet updates to `ADOPTED` and adoption row inserts | Both committed atomically | Pass |
| UAT-08 | Transactional rollback on failure | System (`AdoptionService`) | Trigger post-write failure after `Pet` update | Both `pets` update and `adoptions` insert roll back | Reverted to `AVAILABLE` / `0` rows | Pass |

---

## 3. Defects Found & Fixed

| Defect ID | Symptom & Root Cause | Resolution | Verification |
| :--- | :--- | :--- | :--- |
| DEF-16-01 | Duplicate unique constraint violations (`DataIntegrityViolationException`, e.g., duplicate microchip) fell through to the generic `500 Internal Server Error` handler. | Added `@ExceptionHandler(DataIntegrityViolationException.class)` in `GlobalExceptionHandler` returning sanitized `409 Conflict` JSON. | Verified in `SecurityAndApiIntegrationTest#returns404ForMissingPetAnd409ForDuplicateMicrochip`. |
| DEF-16-02 | Browser requests for `/favicon.ico` triggered `NoResourceFoundException` and logged unhandled `500` responses. | Added `@ExceptionHandler(NoResourceFoundException.class)` in `GlobalExceptionHandler` returning `204 No Content` for `favicon.ico` and `404 Not Found` otherwise. | Verified clean browser DevTools Network logs with zero `500` errors. |

---

## 4. Security & Database Hygiene Checklist

* **Isolated Test Database:** `application-test.properties` targets `pet_adoption_test_db` exclusively (`create-drop`), enforced by a runtime JDBC URL assertion in `AdoptionTransactionMySqlTest`.
* **Secret Management:** All database credentials and role passwords are injected via environment variables (`DB_USERNAME`, `DB_PASSWORD`, `APP_READER_PASSWORD`, `APP_MANAGER_PASSWORD`); `.env` is excluded via `.gitignore` and only `.env.example` is tracked.
* **Sanitized Error Responses:** Unhandled exceptions log a server-side `traceId` without exposing SQL statements, stack traces, passwords, session cookies, or CSRF tokens to clients.