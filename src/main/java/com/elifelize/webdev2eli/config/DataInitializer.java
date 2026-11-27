package com.elifelize.webdev2eli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.elifelize.webdev2eli.entity.AppUser;
import com.elifelize.webdev2eli.entity.Owner;
import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.AppUserRepository;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_READER_PASSWORD:}")
    private String readerPassword;

    @Value("${APP_MANAGER_PASSWORD:}")
    private String managerPassword;

    public DataInitializer(
            PetRepository petRepository,
            OwnerRepository ownerRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!readerPassword.isBlank() && !appUserRepository.existsByUsernameIgnoreCase("volunteer_reader")) {
            appUserRepository.save(new AppUser(
                    "volunteer_reader",
                    passwordEncoder.encode(readerPassword),
                    "ROLE_READER"));
        }

        if (!managerPassword.isBlank() && !appUserRepository.existsByUsernameIgnoreCase("coordinator_mgr")) {
            appUserRepository.save(new AppUser(
                    "coordinator_mgr",
                    passwordEncoder.encode(managerPassword),
                    "ROLE_MANAGER"));
        }

        if (ownerRepository.count() == 0) {
            ownerRepository.save(new Owner("Alex Rivera", "alex@example.com", "555-0101"));
            ownerRepository.save(new Owner("Jamie Cruz", "jamie@example.com", "555-0102"));
        }

        if (petRepository.count() == 0) {
            petRepository.save(new Pet("CHIP-1001", "Buddy", "Dog", "Golden Retriever"));
            petRepository.save(new Pet("CHIP-1002", "Luna", "Cat", "Siamese"));
            petRepository.save(new Pet("CHIP-1003", "Milo", "Dog", "Beagle"));
        }
    }
}