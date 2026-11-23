# Week 14 (F2) — Lecture Notes: Full CRUD & Atomic Workflows

## 1. End-to-End Form-to-API Data Flow
1. **User Interaction:** The user submits an HTML `<form>` (`#petForm`, `#ownerForm`, or `#adoptionForm`).
2. **Client Interception & Guard:** Vanilla JS intercepts the `submit` event with `e.preventDefault()`, disables the submit button to prevent double-submission, and builds a JSON payload from form inputs.
3. **HTTP Request:** The centralized `request(path, options)` helper sends `POST`, `PUT`, or `DELETE` to the Spring Boot REST controller with `Content-Type: application/json`.
4. **Controller & Validation:** Spring Boot deserializes JSON into a DTO (`CreatePetRequestDTO`, `UpdatePetRequestDTO`, `OwnerDTO`, or `AdoptionRequestDTO`) and runs Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@NotNull`).
5. **Service & Persistence:** The `@Service` enforces domain rules (such as duplicate microchip/email detection and referential delete protection) and persists changes through Spring Data JPA to MySQL (`webdev2_pets`).
6. **Response & UI Sync:** `201 Created`, `200 OK`, or `204 No Content` triggers a success banner and refreshes the DOM tables and `<select>` dropdowns; `400`, `404`, or `409` displays a safe `textContent` error banner while keeping user input editable.

## 2. `PUT` vs. `PATCH` Semantics
* **`PUT /api/pets/{id}` & `PUT /api/owners/{id}`:** Full resource replacement. All required fields (`microchipNumber`, `name`, `species`, `status`, `ownerId`) are sent from the edit form so the server state matches the edited representation.
* **`PATCH`:** Partial update reserved for modifying a subset of fields without resending the full resource representation.

## 3. Three-Layer Validation Strategy
1. **Client-Side (UX):** HTML5 `required` and `type="email"` attributes catch missing or malformed inputs immediately before a network round-trip.
2. **Server-Side (`@Valid` + `GlobalExceptionHandler`):** Bean validation annotations (`@NotBlank`, `@Email`, `@NotNull`) return `400 Bad Request` with structured field errors (`ErrorResponse`), preventing malformed API requests (e.g., via `curl` or DevTools) from reaching the database.
3. **Database-Level (MySQL Constraints):** `UNIQUE` constraints on `pets.microchip_number` and `owners.email`, `NOT NULL` columns, and foreign keys (`pets.owner_id`, `adoptions.pet_id`, `adoptions.owner_id`) guarantee integrity at the storage layer.

## 4. Delete Policy & `409 Conflict`
* **Restricted Parent Delete:** `DELETE /api/owners/{id}` checks `petRepository.existsByOwnerId(id)`. If any pet is currently assigned to that owner, the service throws `409 Conflict` instead of leaving orphaned records or throwing an unhandled SQL constraint error.
* **Adopted Pet Protection:** `DELETE /api/pets/{id}` rejects deleting a pet whose status is `ADOPTED` (`409 Conflict`).
* **Empty `204` Handling:** Successful deletes return `204 No Content` with an empty body; `request()` checks `response.status === 204` before calling `response.json()`.

## 5. Atomic Business Workflow (`@Transactional`) & Rollback
* `POST /api/adoptions` invokes `AdoptionService.adoptPet()`, annotated with `@Transactional`.
* Within one database transaction:
  1. The target `Pet` is locked (`PESSIMISTIC_WRITE`) and verified to be `AVAILABLE`.
  2. `pet.status` is updated to `ADOPTED` and `pet.owner` is set (`saveAndFlush`).
  3. A new row is inserted into `adoptions` (`pet_id`, `owner_id`, `adopted_at`, `notes`).
* If any exception occurs after step 2 (such as an already-adopted pet or a simulated post-write failure), Spring rolls back the entire transaction so MySQL never commits a partial state.