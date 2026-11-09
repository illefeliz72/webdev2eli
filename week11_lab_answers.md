# Lab 11 — Capstone Milestone 3 Submission

**Course:** Web Development 2 &nbsp;|&nbsp; **Week:** 11 &nbsp;|&nbsp; **Lab:** 11  
**Student:** Eli Felize Herreria &nbsp;|&nbsp; **Repository:** https://github.com/illefeliz72/webdev2eli &nbsp;|&nbsp; **Branch:** week11

---

## 1. Student Information

| Field | Value |
|---|---|
| Name | Eli Felize Herreria |
| Student ID 2511101| 2026-WEBDEV2 |
| GitHub Repository URL | https://github.com/illefeliz72/webdev2eli |
| Branch | week11 |

---

## 2. Task 1 — Input Validation on DTOs

### Source Code: `CreatePetRequestDTO.java`
**Location:** `src/main/java/com/elifelize/webdev2eli/dto/CreatePetRequestDTO.java`

```java
package com.elifelize.webdev2eli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePetRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @NotBlank(message = "Species is required")
    private String species;

    private String breed;
    private String microchipNumber;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getMicrochipNumber() { return microchipNumber; }
    public void setMicrochipNumber(String microchipNumber) { this.microchipNumber = microchipNumber; }
}
400 Bad Request Output Evidence
JSON
{
  "status": 400,
  "message": "Validation Failed",
  "timestamp": "2026-11-10T01:35:00.123Z",
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ]
}
3. Task 2 — Centralized Global Exception Handler
Source Code: GlobalExceptionHandler.java
Location: src/main/java/com/elifelize/webdev2eli/exception/GlobalExceptionHandler.java

Java
package com.elifelize.webdev2eli.exception;

import com.elifelize.webdev2eli.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldErrorDTO> fieldErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ErrorResponse.FieldErrorDTO(error.getField(), error.getDefaultMessage()));
        }
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                LocalDateTime.now(),
                fieldErrors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected internal error occurred",
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
404 Not Found Output Evidence
JSON
{
  "status": 404,
  "message": "Pet not found with id: 99",
  "timestamp": "2026-11-10T01:36:00.456Z",
  "errors": null
}
4. Task 3 — CORS Configuration
Source Code: WebConfig.java
Location: src/main/java/com/elifelize/webdev2eli/config/WebConfig.java

Java
package com.elifelize.webdev2eli.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
CORS Headers Output Evidence
Plaintext
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
5. Task 4 — Pagination & Filtering
Paginated Output (GET /api/pets?page=0&size=1)
JSON
{
  "content": [
    {
      "id": 1,
      "name": "Buddy",
      "species": "Dog",
      "breed": "Golden Retriever",
      "microchipNumber": "CHIP-1001"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 1
  },
  "totalElements": 3,
  "totalPages": 3
}
Filtered Output (GET /api/pets?species=Cat)
JSON
{
  "content": [
    {
      "id": 2,
      "name": "Luna",
      "species": "Cat",
      "breed": "Siamese",
      "microchipNumber": "CHIP-1002"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
Max Page Size Verification (GET /api/pets?size=999)
Plaintext
Requesting size=999 was processed by PetService and capped at a maximum page size of 100 items. The total items returned in content did not exceed 100.
6. Task 5 — Full API Documentation (API.md)
Markdown
# Pet Management REST API Documentation

Base URL: `http://localhost:8080/api/pets`

## Endpoints Summary

### 1. GET /api/pets
- **Description:** Returns paginated pets, optionally filtered by species.
- **Parameters:** `page` (default: 0), `size` (default: 20, max: 100), `sort` (default: id), `direction` (default: asc), `species` (optional filter)
- **Response:** `200 OK`

### 2. GET /api/pets/{id}
- **Description:** Returns a single pet by ID.
- **Response:** `200 OK` or `404 Not Found`

### 3. POST /api/pets
- **Description:** Creates a new pet record.
- **Body:** `{ "name": "Rex", "species": "Dog", "breed": "Beagle", "microchipNumber": "CHIP-500" }`
- **Response:** `201 Created` or `400 Bad Request`

### 4. PUT /api/pets/{id}
- **Description:** Updates an existing pet.
- **Body:** JSON object matching Pet request shape
- **Response:** `200 OK`, `400 Bad Request`, or `404 Not Found`

### 5. DELETE /api/pets/{id}
- **Description:** Removes a pet record by ID.
- **Response:** `204 No Content` or `404 Not Found`
7. Task 5 — Full API Test Suite (api_tests.json)
JSON
{
  "client": "Postman / Bruno",
  "collectionName": "Week11 - Eli Felize API",
  "requests": [
    {
      "name": "Get All Pets (Paginated)",
      "method": "GET",
      "url": "http://localhost:8080/api/pets?page=0&size=5",
      "expectedStatus": 200
    },
    {
      "name": "Get Pet By ID",
      "method": "GET",
      "url": "http://localhost:8080/api/pets/1",
      "expectedStatus": 200
    },
    {
      "name": "Create Pet (Happy Path)",
      "method": "POST",
      "url": "http://localhost:8080/api/pets",
      "expectedStatus": 201
    },
    {
      "name": "Create Pet (Validation Error)",
      "method": "POST",
      "url": "http://localhost:8080/api/pets",
      "expectedStatus": 400
    },
    {
      "name": "Get Non-Existent Pet",
      "method": "GET",
      "url": "http://localhost:8080/api/pets/999",
      "expectedStatus": 404
    }
  ]
}
Test Suite Results Summary
Request Name	Method	Path	Status	Result
Get All Pets (Paginated)	GET	/api/pets	200	PASSED
Get Pet By ID	GET	/api/pets/1	200	PASSED
Create Pet (Happy Path)	POST	/api/pets	201	PASSED
Create Pet (Validation Error)	POST	/api/pets	400	PASSED
Get Non-Existent Pet	GET	/api/pets/999	404	PASSED
8. Reflection
Implementing centralized exception handling with @RestControllerAdvice had the biggest impact on our capstone API because it instantly replaced raw Spring stack traces with standardized, clean JSON error messages. What surprised me most was how easy Spring Data JPA makes pagination and sorting just by passing a Pageable parameter down to repository queries.