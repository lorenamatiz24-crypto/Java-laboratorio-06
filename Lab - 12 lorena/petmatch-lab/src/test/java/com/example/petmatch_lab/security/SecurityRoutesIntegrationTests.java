package com.example.petmatch_lab.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRoutesIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowAnonymousAccessToLogin() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRedirectAnonymousVisitorToLogin() throws Exception {
        mockMvc.perform(get("/pets"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldForbidRegularUserFromAdminPage() throws Exception {
        mockMvc.perform(get("/admin")
                .with(user("ana@example.com").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToOpenAdminPage() throws Exception {
        mockMvc.perform(get("/admin")
                .with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void shouldForbidRegularUserFromCreatingPet() throws Exception {
        mockMvc.perform(post("/pets")
                .with(user("ana@example.com").roles("USER"))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbidRegularUserFromCompletingRequest() throws Exception {
        mockMvc.perform(post("/pets/7/requests/9/complete")
                .with(user("ana@example.com").roles("USER"))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAdminPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/pets")
                .with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidCsrfToken() throws Exception {
        mockMvc.perform(post("/pets")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf().useInvalidToken()))
            .andExpect(status().isForbidden());
    }
}
