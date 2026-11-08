package com.elifelize.webdev2eli.controller;

import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.service.PetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pet createPet(@RequestBody Pet pet) {
        return petService.registerPet(pet);
    }

    @GetMapping
    public List<Pet> getAllPets() {
        return petService.getAllPets();
    }

    @GetMapping("/{id}")
    public Pet getPetById(@PathVariable Long id) {
        return petService.getPetById(id);
    }

    @PutMapping("/{id}")
    public Pet updatePet(@PathVariable Long id, @RequestBody Pet pet) {
        return petService.updatePet(id, pet);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePet(@PathVariable Long id) {
        petService.removePet(id);
    }

    @GetMapping("/species")
    public List<Pet> getPetsBySpecies(@RequestParam("name") String species) {
        return petService.getPetsBySpecies(species);
    }

    @GetMapping("/search")
    public List<Pet> searchPetsByName(@RequestParam("keyword") String keyword) {
        return petService.searchPetsByName(keyword);
    }

    @GetMapping("/species-list")
    public List<String> getAllSpecies() {
        return petService.getAllSpecies();
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public void batchRegister(@RequestBody List<Pet> pets) {
        petService.batchRegister(pets);
    }
}