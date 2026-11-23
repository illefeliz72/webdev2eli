package com.elifelize.webdev2eli.config;

import com.elifelize.webdev2eli.entity.Owner;
import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public DataInitializer(PetRepository petRepository, OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    @Override
    public void run(String... args) {
        if (ownerRepository.count() == 0) {
            ownerRepository.save(new Owner("Alex Rivera", "alex@example.com", "555-0101"));
            ownerRepository.save(new Owner("Jamie Cruz", "jamie@example.com", "555-0102"));
        }

        if (petRepository.count() == 0) {
            Pet pet1 = new Pet("CHIP-1001", "Buddy", "Dog", "Golden Retriever");
            petRepository.save(pet1);

            Pet pet2 = new Pet("CHIP-1002", "Luna", "Cat", "Siamese");
            petRepository.save(pet2);

            Pet pet3 = new Pet("CHIP-1003", "Milo", "Dog", "Beagle");
            petRepository.save(pet3);
        }
    }
}