package com.elifelize.webdev2eli.service;

import com.elifelize.webdev2eli.dto.CreatePetRequestDTO;
import com.elifelize.webdev2eli.dto.PetResponseDTO;
import com.elifelize.webdev2eli.dto.UpdatePetRequestDTO;
import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    private PetResponseDTO toDTO(Pet pet) {
        return new PetResponseDTO(
                pet.getId(),
                pet.getMicrochipNumber(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed());
    }

    public List<PetResponseDTO> getAllPets() {
        return petRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PetResponseDTO getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id: " + id));
        return toDTO(pet);
    }

    public PetResponseDTO createPet(CreatePetRequestDTO request) {
        if (request.getMicrochipNumber() != null &&
                petRepository.findAll().stream().anyMatch(p -> p.getMicrochipNumber() != null
                        && p.getMicrochipNumber().equalsIgnoreCase(request.getMicrochipNumber()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Microchip number already exists: " + request.getMicrochipNumber());
        }
        Pet pet = new Pet(request.getMicrochipNumber(), request.getName(), request.getSpecies(), request.getBreed());
        Pet saved = petRepository.save(pet);
        return toDTO(saved);
    }

    public PetResponseDTO updatePet(Long id, UpdatePetRequestDTO request) {
        Pet existing = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id: " + id));

        existing.setMicrochipNumber(request.getMicrochipNumber());
        existing.setName(request.getName());
        existing.setSpecies(request.getSpecies());
        existing.setBreed(request.getBreed());

        return toDTO(petRepository.save(existing));
    }

    public PetResponseDTO patchPet(Long id, UpdatePetRequestDTO request) {
        Pet existing = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id: " + id));

        if (request.getMicrochipNumber() != null)
            existing.setMicrochipNumber(request.getMicrochipNumber());
        if (request.getName() != null)
            existing.setName(request.getName());
        if (request.getSpecies() != null)
            existing.setSpecies(request.getSpecies());
        if (request.getBreed() != null)
            existing.setBreed(request.getBreed());

        return toDTO(petRepository.save(existing));
    }

    public void removePet(Long id) {
        if (!petRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id: " + id);
        }
        petRepository.deleteById(id);
    }
}