package com.elifelize.webdev2eli.repository;

import com.elifelize.webdev2eli.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findBySpecies(String species);

    List<Pet> findByNameContainingIgnoreCase(String keyword);

    Optional<Pet> findByMicrochipNumber(String microchipNumber);
}