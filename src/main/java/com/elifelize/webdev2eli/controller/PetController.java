package main.java.com.elifelize.webdev2eli.controller;

import com.elifelize.webdev2eli.model.Pet;
import com.elifelize.webdev2eli.service.PetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Pet> registerPet(@RequestBody Pet pet) {
        return new ResponseEntity<>(petService.registerPet(pet), HttpStatus.CREATED);
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
    public void removePet(@PathVariable Long id) {
        petService.removePet(id);
    }

    @GetMapping("/species")
    public List<Pet> getBySpecies(@RequestParam String name) {
        return petService.getPetsBySpecies(name);
    }

    @GetMapping("/search")
    public List<Pet> searchByName(@RequestParam String keyword) {
        return petService.searchPetsByName(keyword);
    }

    @GetMapping("/species-list")
    public List<String> getSpeciesList() {
        return petService.getAllDistinctSpecies();
    }

    @PostMapping("/batch")
    public ResponseEntity<String> batchRegister(@RequestBody List<Pet> pets) {
        petService.batchRegister(pets);
        return ResponseEntity.status(HttpStatus.CREATED).body("Batch registration successful");
    }
}