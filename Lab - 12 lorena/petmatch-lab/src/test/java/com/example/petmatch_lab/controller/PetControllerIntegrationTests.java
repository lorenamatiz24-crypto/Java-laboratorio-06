package com.example.petmatch_lab.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PetControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRenderCreateForm() throws Exception {
        mockMvc.perform(get("/pets/new").with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/form"))
            .andExpect(model().attributeExists("petForm"));
    }

    @Test
    void shouldRejectInvalidPetFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/pets")
                .param("name", "")
                .param("species", "   ")
                .param("age", "-1")
                .param("description", "Prueba").with(user("admin@example.com").roles("ADMIN")).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/form"))
            .andExpect(model().attributeHasFieldErrors(
                "petForm", "name", "species", "age"));
    }

    @Test
    void shouldRedirectAfterValidCreation() throws Exception {
        mockMvc.perform(post("/pets")
                .param("name", "Kira")
                .param("species", "Perro")
                .param("age", "4")
                .param("description", "Juguetona").with(user("admin@example.com").roles("ADMIN")).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/pets/*"));
    }

    @Test
    void shouldReturnNotFoundWhenEditingMissingPet() throws Exception {
        mockMvc.perform(get("/pets/999999/edit").with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingPet() throws Exception {
        mockMvc.perform(post("/pets/999999/delete").with(user("admin@example.com").roles("ADMIN")).with(csrf()))
            .andExpect(status().isNotFound());
    }
}
