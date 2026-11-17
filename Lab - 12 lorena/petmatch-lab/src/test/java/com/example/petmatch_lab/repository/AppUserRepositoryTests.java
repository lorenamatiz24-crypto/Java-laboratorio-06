package com.example.petmatch_lab.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.petmatch_lab.model.AppUser;
import com.example.petmatch_lab.model.UserRole;

@DataJpaTest
class AppUserRepositoryTests {

    @Autowired
    private AppUserRepository repository;

    @Test
    void shouldFindPersistedUserByEmail() {
        AppUser saved = repository.saveAndFlush(
            new AppUser("Ana", "ana@example.com", "{bcrypt}test-value")
        );

        var result = repository.findByEmail("ana@example.com");

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.orElseThrow().getId());
        assertEquals(UserRole.USER, result.orElseThrow().getRole());
    }
}
