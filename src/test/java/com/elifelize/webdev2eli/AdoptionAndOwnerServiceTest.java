package com.elifelize.webdev2eli;

import com.elifelize.webdev2eli.dto.AdoptionRequestDTO;
import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.AdoptionRepository;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;
import com.elifelize.webdev2eli.service.AdoptionService;
import com.elifelize.webdev2eli.service.OwnerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdoptionAndOwnerServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private AdoptionRepository adoptionRepository;

    @InjectMocks
    private AdoptionService adoptionService;

    @InjectMocks
    private OwnerService ownerService;

    @Test
    void rejectsAdoptionWhenPetIsAlreadyAdopted() {
        Pet adoptedPet = new Pet();
        adoptedPet.setId(10L);
        adoptedPet.setName("Buddy");
        adoptedPet.setStatus("ADOPTED");

        AdoptionRequestDTO request = new AdoptionRequestDTO();
        request.setPetId(10L);
        request.setOwnerId(1L);

        when(petRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(adoptedPet));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> adoptionService.adoptPet(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(petRepository, never()).saveAndFlush(any());
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void rejectsAdoptionWhenPetIdDoesNotExist() {
        AdoptionRequestDTO request = new AdoptionRequestDTO();
        request.setPetId(999L);
        request.setOwnerId(1L);

        when(petRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> adoptionService.adoptPet(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void rejectsOwnerDeletionWhenOwnerStillHasAssignedPets() {
        when(ownerRepository.existsById(5L)).thenReturn(true);
        when(petRepository.existsByOwnerId(5L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ownerService.deleteOwner(5L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(ownerRepository, never()).deleteById(any());
    }
}