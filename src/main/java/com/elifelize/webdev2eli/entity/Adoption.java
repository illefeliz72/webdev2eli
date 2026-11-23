package com.elifelize.webdev2eli.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adoptions")
public class Adoption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "adopted_at", nullable = false)
    private LocalDateTime adoptedAt;

    private String notes;

    public Adoption() {
    }

    public Adoption(Pet pet, Owner owner, LocalDateTime adoptedAt, String notes) {
        this.pet = pet;
        this.owner = owner;
        this.adoptedAt = adoptedAt;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public LocalDateTime getAdoptedAt() {
        return adoptedAt;
    }

    public void setAdoptedAt(LocalDateTime adoptedAt) {
        this.adoptedAt = adoptedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}