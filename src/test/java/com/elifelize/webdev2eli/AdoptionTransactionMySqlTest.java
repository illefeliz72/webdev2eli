package com.elifelize.webdev2eli;

import com.elifelize.webdev2eli.dto.AdoptionRequestDTO;
import com.elifelize.webdev2eli.entity.Owner;
import com.elifelize.webdev2eli.entity.Pet;
import com.elifelize.webdev2eli.repository.AdoptionRepository;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;
import com.elifelize.webdev2eli.service.AdoptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Intentionally NOT annotated with @Transactional at class or method level.
 * Leaving @Transactional off ensures AdoptionService.adoptPet(...) runs inside
 * its
 * real Spring proxy transaction boundary and commits/rolls back to MySQL before
 * JdbcTemplate inspects the independent post-execution database state.
 */
@SpringBootTest
@ActiveProfiles("test")
class AdoptionTransactionMySqlTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdoptionService adoptionService;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AdoptionRepository adoptionRepository;

    @BeforeEach
    void verifyDisposableTestDatabaseAndCleanState() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL();
            assertTrue(url.contains("pet_adoption_test_db"),
                    "Safety check failed: tests must only run against pet_adoption_test_db, got: " + url);
        }
        adoptionRepository.deleteAll();
        petRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    @Test
    void commitsPetStatusUpdateAndAdoptionInsertAtomicallyInMySql() {
        Pet pet = petRepository.save(new Pet("CHIP-TX-COMMIT", "Biscuit", "Dog", "Corgi"));

        Owner owner = new Owner("Jordan Lee", "jordan.commit@example.com", "555-0201");
        owner.setCreatedBy("coordinator_mgr");
        owner = ownerRepository.save(owner);

        AdoptionRequestDTO request = new AdoptionRequestDTO();
        request.setPetId(pet.getId());
        request.setOwnerId(owner.getId());
        request.setNotes("Home visit verified");
        request.setSimulateFailureAfterPetUpdate(false);

        // Act — invokes real Spring @Transactional proxy
        adoptionService.adoptPet(request);

        // Assert — independent SQL queries verify committed MySQL state
        String dbStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM pets WHERE id = ?", String.class, pet.getId());
        Long dbOwnerId = jdbcTemplate.queryForObject(
                "SELECT owner_id FROM pets WHERE id = ?", Long.class, pet.getId());
        Integer adoptionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM adoptions WHERE pet_id = ? AND owner_id = ?",
                Integer.class, pet.getId(), owner.getId());

        assertEquals("ADOPTED", dbStatus);
        assertEquals(owner.getId(), dbOwnerId);
        assertEquals(1, adoptionCount);
    }

    @Test
    void rollsBackBothPetUpdateAndAdoptionInsertOnPostWriteFailureInMySql() {
        Pet pet = petRepository.save(new Pet("CHIP-TX-ROLLBACK", "Mochi", "Cat", "Ragdoll"));

        Owner owner = new Owner("Casey Park", "casey.rollback@example.com", "555-0202");
        owner.setCreatedBy("coordinator_mgr");
        owner = ownerRepository.save(owner);

        AdoptionRequestDTO request = new AdoptionRequestDTO();
        request.setPetId(pet.getId());
        request.setOwnerId(owner.getId());
        request.setNotes("Simulated post-write failure");
        request.setSimulateFailureAfterPetUpdate(true);

        final Long petId = pet.getId();

        // Act — simulate post-write exception after Pet update and flush
        assertThrows(ResponseStatusException.class, () -> adoptionService.adoptPet(request));

        // Assert — independent SQL queries confirm BOTH tables reverted to
        // pre-transaction state
        String dbStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM pets WHERE id = ?", String.class, petId);
        Long dbOwnerId = jdbcTemplate.queryForObject(
                "SELECT owner_id FROM pets WHERE id = ?", Long.class, petId);
        Integer adoptionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM adoptions WHERE pet_id = ?", Integer.class, petId);

        assertEquals("AVAILABLE", dbStatus);
        assertNull(dbOwnerId);
        assertEquals(0, adoptionCount);
    }
}
