package com.example.petmatch_lab.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.petmatch_lab.model.Pet;

import jakarta.persistence.EntityManager;

@DataJpaTest
class PetRepositoryTests {

    @Autowired
    private PetRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldGenerateIdentifierWhenSavingNewPet() {
        Pet pet = new Pet("Kira", "Dog", 4, "Very playful.");

        Pet saved = repository.saveAndFlush(pet);

        assertNotNull(saved.getId());
        assertEquals("Kira", saved.getName());
    }

    @Test
    void shouldFindPersistedPetByIdentifier() {
        Pet saved = repository.saveAndFlush(
            new Pet("Luna", "Dog", 3, "Enjoys walks.")
        );
        Long id = saved.getId();
        entityManager.clear();

        Pet found = repository.findById(id).orElseThrow();

        assertEquals("Luna", found.getName());
        assertEquals(3, found.getAge());
    }

    @Test
    void shouldPersistChangesToExistingPet() {
        Pet saved = repository.saveAndFlush(
            new Pet("Milo", "Cat", 2, null)
        );
        Long id = saved.getId();

        saved.updateDetails("Milo", "Cat", 5, null);
        repository.saveAndFlush(saved);
        entityManager.clear();

        Pet found = repository.findById(id).orElseThrow();

        assertEquals(5, found.getAge());
    }

    @Test
    void shouldDeletePersistedPet() {
        Pet saved = repository.saveAndFlush(
            new Pet("Nube", "Rabbit", 1, null)
        );
        Long id = saved.getId();

        repository.delete(saved);
        repository.flush();
        entityManager.clear();

        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    void shouldReturnEmptyWhenIdentifierDoesNotExist() {
        assertTrue(repository.findById(Long.MAX_VALUE).isEmpty());
    }
}
