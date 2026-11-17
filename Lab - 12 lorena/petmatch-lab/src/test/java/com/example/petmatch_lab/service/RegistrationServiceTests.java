package com.example.petmatch_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.petmatch_lab.dto.RegistrationForm;
import com.example.petmatch_lab.exception.DuplicateEmailException;
import com.example.petmatch_lab.model.AppUser;
import com.example.petmatch_lab.model.UserRole;
import com.example.petmatch_lab.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTests {

    private static final String PASSWORD = "ExamplePassword123!";

    @Mock
    private AppUserRepository repository;

    private PasswordEncoder passwordEncoder;
    private RegistrationService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        service = new RegistrationService(repository, passwordEncoder);
    }

    @Test
    void shouldNormalizeEmailHashPasswordAndAssignUserRole() {
        RegistrationForm form = form("  ANA@EXAMPLE.COM  ", PASSWORD);
        when(repository.save(any(AppUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser created = service.register(form);

        assertEquals("ana@example.com", created.getEmail());
        assertEquals("Ana", created.getDisplayName());
        assertEquals(UserRole.USER, created.getRole());
        assertTrue(created.isEnabled());
        assertFalse(PASSWORD.equals(created.getPasswordHash()));
        assertTrue(passwordEncoder.matches(PASSWORD, created.getPasswordHash()));
        verify(repository).existsByEmail("ana@example.com");
    }

    @Test
    void shouldRejectDuplicateEmailBeforeSaving() {
        RegistrationForm form = form("ana@example.com", PASSWORD);
        when(repository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class,
            () -> service.register(form));

        verify(repository, never()).save(any(AppUser.class));
    }

    @Test
    void shouldRejectDifferentConfirmationPassword() {
        RegistrationForm form = form("ana@example.com", PASSWORD);
        form.setConfirmPassword("DifferentPassword123!");

        assertThrows(IllegalArgumentException.class,
            () -> service.register(form));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectShortPassword() {
        RegistrationForm form = form("ana@example.com", "short");

        assertThrows(IllegalArgumentException.class,
            () -> service.register(form));

        verifyNoInteractions(repository);
    }

    private RegistrationForm form(String email, String password) {
        RegistrationForm form = new RegistrationForm();
        form.setDisplayName("  Ana  ");
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(password);
        return form;
    }
}
