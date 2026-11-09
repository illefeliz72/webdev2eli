package com.elifelize.webdev2eli.config;

import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PetRepository petRepository;

    public DataInitializer(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public void run(String... args) {
        if (petRepository.count() == 0) {
            Pet pet1 = new Pet();
            pet1.setName("Buddy");
            pet1.setSpecies("Dog");
            pet1.setBreed("Golden Retriever");
            pet1.setMicrochipNumber("CHIP-1001");
            petRepository.save(pet1);

            Pet pet2 = new Pet();
            pet2.setName("Luna");
            pet2.setSpecies("Cat");
            pet2.setBreed("Siamese");
            pet2.setMicrochipNumber("CHIP-1002");
            petRepository.save(pet2);

            Pet pet3 = new Pet();
            pet3.setName("Milo");
            pet3.setSpecies("Dog");
            pet3.setBreed("Beagle");
            pet3.setMicrochipNumber("CHIP-1003");
            petRepository.save(pet3);
        }
    }
}