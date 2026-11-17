package com.example.petmatch_lab.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petmatch_lab.model.UserRole;
import com.example.petmatch_lab.repository.AppUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerIntegrationTests {

    private static final String EMAIL = "registration-test@example.com";
    private static final String PASSWORD = "ExamplePassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterUserWithEncodedPasswordAndDefaultRole() throws Exception {
        userRepository.findByEmail(EMAIL).ifPresent(userRepository::delete);

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("displayName", "Ana")
                .param("email", EMAIL)
                .param("password", PASSWORD)
                .param("confirmPassword", PASSWORD))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?registered"));

        var stored = userRepository.findByEmail(EMAIL).orElseThrow();
        assertEquals(UserRole.USER, stored.getRole());
        assertFalse(PASSWORD.equals(stored.getPasswordHash()));
        assertTrue(passwordEncoder.matches(PASSWORD, stored.getPasswordHash()));
    }

    @Test
    void shouldRejectRegistrationWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/register")
                .param("displayName", "Ana")
                .param("email", "other-registration@example.com")
                .param("password", PASSWORD)
                .param("confirmPassword", PASSWORD))
            .andExpect(status().isForbidden());
    }
}
