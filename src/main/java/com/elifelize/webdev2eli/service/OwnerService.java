package com.elifelize.webdev2eli.service;

import com.elifelize.webdev2eli.dto.OwnerDTO;
import com.elifelize.webdev2eli.entity.Owner;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;

    public OwnerService(OwnerRepository ownerRepository, PetRepository petRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
    }

    private OwnerDTO toDTO(Owner owner) {
        return new OwnerDTO(owner.getId(), owner.getFullName(), owner.getEmail(), owner.getPhone());
    }

    public List<OwnerDTO> getAllOwners() {
        return ownerRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public OwnerDTO getOwnerById(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));
        return toDTO(owner);
    }

    public OwnerDTO createOwner(OwnerDTO request) {
        if (ownerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered: " + request.getEmail());
        }
        Owner saved = ownerRepository.save(new Owner(request.getFullName(), request.getEmail(), request.getPhone()));
        return toDTO(saved);
    }

    public OwnerDTO updateOwner(Long id, OwnerDTO request) {
        Owner existing = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));

        if (!existing.getEmail().equalsIgnoreCase(request.getEmail())
                && ownerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered: " + request.getEmail());
        }

        existing.setFullName(request.getFullName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        return toDTO(ownerRepository.save(existing));
    }

    public void deleteOwner(Long id) {
        if (!ownerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id);
        }
        if (petRepository.existsByOwnerId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete owner with assigned pets. Reassign or remove their pets first.");
        }
        ownerRepository.deleteById(id);
    }
}