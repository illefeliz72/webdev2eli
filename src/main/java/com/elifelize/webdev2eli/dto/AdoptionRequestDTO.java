package com.elifelize.webdev2eli.dto;

import jakarta.validation.constraints.NotNull;

public class AdoptionRequestDTO {

    @NotNull(message = "Pet ID is required")
    private Long petId;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    private String notes;

    // Used only to prove atomic rollback in Week 14 testing
    private boolean simulateFailureAfterPetUpdate = false;

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isSimulateFailureAfterPetUpdate() { return simulateFailureAfterPetUpdate; }
    public void setSimulateFailureAfterPetUpdate(boolean simulateFailureAfterPetUpdate) {
        this.simulateFailureAfterPetUpdate = simulateFailureAfterPetUpdate;
    }
}