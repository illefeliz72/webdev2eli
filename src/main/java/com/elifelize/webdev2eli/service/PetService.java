package com.elifelize.webdev2eli.service;

import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public Pet registerPet(Pet pet) {
        return petRepository.save(pet);
    }

    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public Pet getPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id: " + id));
    }

    public Pet updatePet(Long id, Pet updated) {
        Pet existing = getPetById(id);
        existing.setMicrochipNumber(updated.getMicrochipNumber());
        existing.setName(updated.getName());
        existing.setSpecies(updated.getSpecies());
        existing.setBreed(updated.getBreed());
        return petRepository.save(existing);
    }

    public void removePet(Long id) {
        Pet existing = getPetById(id);
        petRepository.deleteById(existing.getId());
    }

    public List<Pet> getPetsBySpecies(String species) {
        return petRepository.findBySpeciesIgnoreCase(species);
    }

    public List<Pet> searchPetsByName(String keyword) {
        return petRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<String> getAllSpecies() {
        return petRepository.findAllSpecies();
    }

    @Transactional
    public void batchRegister(List<Pet> pets) {
        for (Pet p : pets) {
            petRepository.save(p);
        }
    }
}