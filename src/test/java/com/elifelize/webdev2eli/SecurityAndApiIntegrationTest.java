package com.elifelize.webdev2eli;

import com.elifelize.webdev2eli.repository.AdoptionRepository;
import com.elifelize.webdev2eli.repository.OwnerRepository;
import com.elifelize.webdev2eli.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // Keeps the full Spring Security filter chain active
@ActiveProfiles("test")
class SecurityAndApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdoptionRepository adoptionRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @BeforeEach
    void resetTestTables() {
        adoptionRepository.deleteAll();
        petRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    @Test
    void deniesAnonymousReadWith401Json() throws Exception {
        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "volunteer_reader", roles = "READER")
    void deniesReaderMutationEvenWithValidCsrfToken() throws Exception {
        String payload = """
                {
                  "microchipNumber": "CHIP-READER-BLOCK",
                  "name": "BlockedPet",
                  "species": "Dog",
                  "breed": "Beagle"
                }
                """;

        mockMvc.perform(post("/api/pets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coordinator_mgr", roles = "MANAGER")
    void allowsManagerCreateWithValidCsrfAndReturns201() throws Exception {
        String payload = """
                {
                  "microchipNumber": "CHIP-API-201",
                  "name": "Nova",
                  "species": "Cat",
                  "breed": "Calico"
                }
                """;

        mockMvc.perform(post("/api/pets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.microchipNumber").value("CHIP-API-201"))
                .andExpect(jsonPath("$.name").value("Nova"));
    }

    @Test
    @WithMockUser(username = "coordinator_mgr", roles = "MANAGER")
    void rejectsInvalidBlankPetPayloadWith400() throws Exception {
        String invalidPayload = """
                {
                  "microchipNumber": "",
                  "name": "",
                  "species": ""
                }
                """;

        mockMvc.perform(post("/api/pets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(username = "coordinator_mgr", roles = "MANAGER")
    void returns404ForMissingPetAnd409ForDuplicateMicrochip() throws Exception {
        // 1. Missing ID -> 404 Not Found
        mockMvc.perform(get("/api/pets/999999"))
                .andExpect(status().isNotFound());

        // 2. Duplicate unique microchip -> 409 Conflict (instead of raw 500)
        String firstPet = """
                {
                  "microchipNumber": "CHIP-DUP-409",
                  "name": "First",
                  "species": "Dog",
                  "breed": "Husky"
                }
                """;
        mockMvc.perform(post("/api/pets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstPet))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/pets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstPet))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}