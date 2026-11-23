package com.elifelize.webdev2eli.repository;

import com.elifelize.webdev2eli.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
    Page<Pet> findBySpecies(String species, Pageable pageable);
}