# Pet Management REST API

Base URL: `http://localhost:8080/api/pets`

## Endpoints

### 1. Get All Pets (Paginated & Filtered)
- **URL:** `/api/pets`
- **Method:** `GET`
- **Query Parameters:**
  - `page` (default: 0)
  - `size` (default: 10)
  - `sort` (default: "id")
  - `direction` (default: "asc")
  - `species` (optional filter)
- **Response:** `200 OK` with Page object.

### 2. Get Pet by ID
- **URL:** `/api/pets/{id}`
- **Method:** `GET`
- **Response:** `200 OK` or `404 Not Found`.

### 3. Create Pet
- **URL:** `/api/pets`
- **Method:** `POST`
- **Request Body:** JSON payload (`name`, `species`, `age`, `breed`, `microchipNumber`)
- **Response:** `201 Created` or `400 Bad Request`.

### 4. Update Pet
- **URL:** `/api/pets/{id}`
- **Method:** `PUT`
- **Request Body:** JSON payload
- **Response:** `200 OK` or `404 Not Found`.

### 5. Delete Pet
- **URL:** `/api/pets/{id}`
- **Method:** `DELETE`
- **Response:** `204 No Content` or `404 Not Found`.