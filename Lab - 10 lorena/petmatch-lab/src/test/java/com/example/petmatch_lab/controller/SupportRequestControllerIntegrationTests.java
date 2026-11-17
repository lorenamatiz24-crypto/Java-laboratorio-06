package com.example.petmatch_lab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class SupportRequestControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SupportRequestRepository requestRepository;

    @Test
    void shouldReturnNotFoundForUnknownPet() throws Exception {
        mockMvc.perform(get("/pets/{petId}/requests", Long.MAX_VALUE))
            .andExpect(status().isNotFound())
            .andExpect(view().name("errors/status"));
    }

    @Test
    void shouldRejectInvalidRequestForm() throws Exception {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Kira", "Dog", 4, null)
        );

        mockMvc.perform(post("/pets/{petId}/requests", pet.getId())
                .param("type", "")
                .param("description", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/form"));
    }

    @Test
    void shouldRedirectAfterValidRequestCreation() throws Exception {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Kira", "Dog", 4, null)
        );

        mockMvc.perform(post("/pets/{petId}/requests", pet.getId())
                .param("type", "WALK")
                .param("description", "Necesita un paseo corto."))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/pets/" + pet.getId() + "/requests"));
    }

    @Test
    void shouldShowFormErrorForSecondOpenRequest() throws Exception {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Luna", "Dog", 3, null)
        );
        requestRepository.saveAndFlush(
            new SupportRequest(pet, SupportType.WALK, "Primera")
        );

        mockMvc.perform(post("/pets/{petId}/requests", pet.getId())
                .param("type", "FEEDING")
                .param("description", "Segunda"))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/form"))
            .andExpect(model().attributeHasErrors("supportRequestForm"));
    }

    @Test
    void shouldReturnConflictForInvalidStateTransition() throws Exception {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Nube", "Rabbit", 1, null)
        );
        SupportRequest request = new SupportRequest(
            pet, SupportType.COMPANY, "Needs company"
        );
        request.cancel();
        SupportRequest saved = requestRepository.saveAndFlush(request);

        mockMvc.perform(post(
                "/pets/{petId}/requests/{requestId}/complete",
                pet.getId(), saved.getId()
            ))
            .andExpect(status().isConflict())
            .andExpect(view().name("errors/status"));
    }

    @Test
    void shouldReturnNotFoundForRequestOfAnotherPet() throws Exception {
        Pet owner = petRepository.saveAndFlush(
            new Pet("Milo", "Cat", 2, null)
        );
        Pet other = petRepository.saveAndFlush(
            new Pet("Sol", "Cat", 1, null)
        );
        SupportRequest saved = requestRepository.saveAndFlush(
            new SupportRequest(owner, SupportType.COMPANY, "Compañía")
        );

        mockMvc.perform(post(
                "/pets/{petId}/requests/{requestId}/complete",
                other.getId(), saved.getId()
            ))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRedirectAfterReplacingOpenRequest() throws Exception {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Toby", "Turtle", 8, null)
        );
        SupportRequest saved = requestRepository.saveAndFlush(
            new SupportRequest(pet, SupportType.WALK, "Original")
        );

        mockMvc.perform(post(
                "/pets/{petId}/requests/{requestId}/replace",
                pet.getId(), saved.getId()
            )
                .param("type", "FEEDING")
                .param("description", "Reemplazo"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/pets/" + pet.getId() + "/requests"));
    }

    @Test
    void shouldRejectDeletingPetWithRequests() throws Exception {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Bebe", "Hamster", 4, null)
        );
        requestRepository.saveAndFlush(
            new SupportRequest(pet, SupportType.FEEDING, "Comida")
        );

        mockMvc.perform(post("/pets/{id}/delete", pet.getId()))
            .andExpect(status().isConflict())
            .andExpect(view().name("errors/status"));
    }
}
