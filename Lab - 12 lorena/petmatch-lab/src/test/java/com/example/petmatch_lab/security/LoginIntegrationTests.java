package com.example.petmatch_lab.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petmatch_lab.model.AppUser;
import com.example.petmatch_lab.repository.AppUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class LoginIntegrationTests {

    private static final String EMAIL = "login-test@example.com";
    private static final String PASSWORD = "ExamplePassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepareUser() {
        userRepository.findByEmail(EMAIL)
            .ifPresent(userRepository::delete);

        userRepository.saveAndFlush(new AppUser(
            "Test User", EMAIL, passwordEncoder.encode(PASSWORD)
        ));
    }

    @Test
    void shouldAuthenticateRegisteredUserWithCorrectPassword() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", EMAIL)
                .param("password", PASSWORD))
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated().withUsername(EMAIL));
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", EMAIL)
                .param("password", "WrongPassword123!"))
            .andExpect(status().is3xxRedirection())
            .andExpect(unauthenticated());
    }
}
