package com.elifelize.webdev2eli.service;

import com.elifelize.webdev2eli.dto.AdoptionRequestDTO;
import com.elifelize.webdev2eli.dto.AdoptionResponseDTO;
import com.elifelize.webdev2eli.entity.Adoption;
import com.elifelize.webdev2eli.entity.Owner;
import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.AdoptionRepository;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdoptionService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;
    private final AdoptionRepository adoptionRepository;

    public AdoptionService(PetRepository petRepository, OwnerRepository ownerRepository,
            AdoptionRepository adoptionRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
        this.adoptionRepository = adoptionRepository;
    }

    public List<AdoptionResponseDTO> getAllAdoptions() {
        return adoptionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdoptionResponseDTO adoptPet(AdoptionRequestDTO request) {
        // 1. Lock the Pet row to prevent double-adoption race conditions
        Pet pet = petRepository.findByIdForUpdate(request.getPetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pet not found with id: " + request.getPetId()));

        if (!"AVAILABLE".equalsIgnoreCase(pet.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pet '" + pet.getName() + "' is already adopted and cannot be adopted again.");
        }

        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Owner not found with id: " + request.getOwnerId()));

        // 2. First write: update Pet status & owner
        pet.setStatus("ADOPTED");
        pet.setOwner(owner);
        petRepository.saveAndFlush(pet);

        // 3. Controlled post-write failure hook to prove @Transactional rollback in
        // MySQL
        if (request.isSimulateFailureAfterPetUpdate()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Simulated post-write failure: rolling back pet status update and adoption record.");
        }

        // 4. Second write: insert Adoption audit record
        Adoption adoption = new Adoption(pet, owner, LocalDateTime.now(), request.getNotes());
        Adoption saved = adoptionRepository.save(adoption);

        return toDTO(saved);
    }

    private AdoptionResponseDTO toDTO(Adoption a) {
        return new AdoptionResponseDTO(
                a.getId(),
                a.getPet().getId(),
                a.getPet().getName(),
                a.getOwner().getId(),
                a.getOwner().getFullName(),
                a.getAdoptedAt(),
                a.getNotes());
    }
}