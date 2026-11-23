package com.elifelize.webdev2eli.controller;

import com.elifelize.webdev2eli.dto.CreatePetRequestDTO;
import com.elifelize.webdev2eli.dto.PetResponseDTO;
import com.elifelize.webdev2eli.service.PetService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> getAllPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String species
    ) {
        if (size > 100) {
            size = 100;
        }
        return ResponseEntity.ok(petService.getAllPets(page, size, sort, direction, species));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetById(id));
    }

    @PostMapping
    public ResponseEntity<PetResponseDTO> createPet(@Valid @RequestBody CreatePetRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.createPet(request));
    }
}