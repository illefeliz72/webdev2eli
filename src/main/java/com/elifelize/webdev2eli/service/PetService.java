package com.elifelize.webdev2eli.service;

import com.elifelize.webdev2eli.dto.CreatePetRequestDTO;
import com.elifelize.webdev2eli.dto.PetResponseDTO;
import com.elifelize.webdev2eli.dto.UpdatePetRequestDTO;
import com.elifelize.webdev2eli.entity.Owner;
import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public PetService(PetRepository petRepository, OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    private PetResponseDTO toDTO(Pet pet) {
        Long ownerId = pet.getOwner() != null ? pet.getOwner().getId() : null;
        String ownerName = pet.getOwner() != null ? pet.getOwner().getFullName() : null;
        return new PetResponseDTO(
                pet.getId(),
                pet.getMicrochipNumber(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getStatus(),
                ownerId,
                ownerName);
    }

    private Owner resolveOwner(Long ownerId) {
        if (ownerId == null) {
            return null;
        }
        return ownerRepository.findById(ownerId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + ownerId));
    }

    public List<PetResponseDTO> getAllPets(int page, int size, String sort, String direction, String species) {
        return petRepository.findAll().stream()
                .filter(p -> species == null || species.isBlank() ||
                        (p.getSpecies() != null && p.getSpecies().equalsIgnoreCase(species)))
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
        Owner owner = resolveOwner(request.getOwnerId());
        pet.setOwner(owner);
        pet.setStatus(owner != null ? "ADOPTED" : "AVAILABLE");
        return toDTO(petRepository.save(pet));
    }

    public PetResponseDTO updatePet(Long id, UpdatePetRequestDTO request) {
        Pet existing = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id: " + id));

        if (request.getMicrochipNumber() != null &&
                petRepository.findAll().stream().anyMatch(p -> !p.getId().equals(id)
                        && p.getMicrochipNumber() != null
                        && p.getMicrochipNumber().equalsIgnoreCase(request.getMicrochipNumber()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Microchip number already exists: " + request.getMicrochipNumber());
        }

        existing.setMicrochipNumber(request.getMicrochipNumber());
        existing.setName(request.getName());
        existing.setSpecies(request.getSpecies());
        existing.setBreed(request.getBreed());

        Owner owner = resolveOwner(request.getOwnerId());
        existing.setOwner(owner);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            existing.setStatus(request.getStatus());
        } else {
            existing.setStatus(owner != null ? "ADOPTED" : "AVAILABLE");
        }

        return toDTO(petRepository.save(existing));
    }

    public void removePet(Long id) {
        Pet existing = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id: " + id));
        if ("ADOPTED".equalsIgnoreCase(existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete an adopted pet. Unassign or update status first.");
        }
        petRepository.deleteById(id);
    }
}