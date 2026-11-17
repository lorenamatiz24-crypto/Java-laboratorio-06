package com.example.petmatch_lab.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.petmatch_lab.model.AppUser;
import com.example.petmatch_lab.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTests {

    @Mock
    private AppUserRepository repository;

    private DatabaseUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new DatabaseUserDetailsService(repository);
    }

    @Test
    void shouldLoadNormalizedEmailAndRole() {
        AppUser user = new AppUser(
            "Ana", "ana@example.com", "{bcrypt}example-hash"
        );
        when(repository.findByEmail("ana@example.com"))
            .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername(" ANA@EXAMPLE.COM ");

        assertEquals("ana@example.com", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(
            authority -> authority.getAuthority().equals("ROLE_USER")
        ));
        assertTrue(details.isEnabled());
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(repository.findByEmail("missing@example.com"))
            .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> service.loadUserByUsername("missing@example.com"));
    }
}
