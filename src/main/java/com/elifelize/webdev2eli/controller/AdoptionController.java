package com.elifelize.webdev2eli.controller;

import com.elifelize.webdev2eli.dto.AdoptionRequestDTO;
import com.elifelize.webdev2eli.dto.AdoptionResponseDTO;
import com.elifelize.webdev2eli.service.AdoptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adoptions")
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @GetMapping
    public ResponseEntity<List<AdoptionResponseDTO>> getAllAdoptions() {
        return ResponseEntity.ok(adoptionService.getAllAdoptions());
    }

    @PostMapping
    public ResponseEntity<AdoptionResponseDTO> adoptPet(@Valid @RequestBody AdoptionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adoptionService.adoptPet(request));
    }
}