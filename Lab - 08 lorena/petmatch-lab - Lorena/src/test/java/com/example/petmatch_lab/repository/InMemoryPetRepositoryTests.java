package com.example.petmatch_lab.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryPetRepositoryTests {

    private InMemoryPetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPetRepository();
    }

    @Test
    void shouldCreatePetWithNewIdentifier() {
        var created = repository.create("Kira", "Perro", 4, "Juguetona");

        assertEquals(4L, created.getId());
        assertEquals("Kira", repository.findById(4L).orElseThrow().getName());
        assertEquals(4, repository.findAll().size());
    }

    @Test
    void shouldAssignDifferentIdentifiersToTwoNewPets() {
        var first = repository.create("Kira", "Perro", 4, null);
        var second = repository.create("Sol", "Gato", 1, null);

        assertTrue(first.getId() != second.getId());
    }

    @Test
    void shouldUpdateExistingPetWithoutChangingIdentifier() {
        var updated = repository.update(2L, "Milo", "Gato", 3, null);

        assertTrue(updated.isPresent());
        assertEquals(2L, updated.orElseThrow().getId());
        assertEquals(3, repository.findById(2L).orElseThrow().getAge());
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingPet() {
        var updated = repository.update(999L, "X", "Gato", 1, null);

        assertTrue(updated.isEmpty());
        assertEquals(3, repository.findAll().size());
    }

    @Test
    void shouldDeleteOnlyRequestedPet() {
        assertTrue(repository.delete(2L));

        assertTrue(repository.findById(2L).isEmpty());
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void shouldReportMissingPetOnDeletion() {
        assertFalse(repository.delete(999L));
    }
}