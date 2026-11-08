package com.elifelize.webdev2eli.controller;

import com.elifelize.webdev2eli.dto.CreatePetRequestDTO;
import com.elifelize.webdev2eli.dto.PetResponseDTO;
import com.elifelize.webdev2eli.dto.UpdatePetRequestDTO;
import com.elifelize.webdev2eli.service.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> getAllPets() {
        return ResponseEntity.ok(petService.getAllPets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetById(id));
    }

    @PostMapping
    public ResponseEntity<PetResponseDTO> createPet(@RequestBody CreatePetRequestDTO request) {
        PetResponseDTO created = petService.createPet(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> updatePet(@PathVariable Long id, @RequestBody UpdatePetRequestDTO request) {
        return ResponseEntity.ok(petService.updatePet(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PetResponseDTO> patchPet(@PathVariable Long id, @RequestBody UpdatePetRequestDTO request) {
        return ResponseEntity.ok(petService.patchPet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.removePet(id);
        return ResponseEntity.noContent().build();
    }
}