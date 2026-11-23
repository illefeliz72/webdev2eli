package com.elifelize.webdev2eli.dto;

public class PetResponseDTO {

    private Long id;
    private String microchipNumber;
    private String name;
    private String species;
    private String breed;
    private String status;
    private Long ownerId;
    private String ownerName;

    public PetResponseDTO() {
    }

    public PetResponseDTO(Long id, String microchipNumber, String name, String species, String breed, String status,
            Long ownerId, String ownerName) {
        this.id = id;
        this.microchipNumber = microchipNumber;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.status = status;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}