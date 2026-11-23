package com.elifelize.webdev2eli.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePetRequestDTO {

    @NotBlank(message = "Microchip number is required")
    private String microchipNumber;

    @NotBlank(message = "Pet name is required")
    private String name;

    @NotBlank(message = "Species is required")
    private String species;

    private String breed;
    private Long ownerId;

    public String getMicrochipNumber() {
        return microchipNumber;
    }

    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}