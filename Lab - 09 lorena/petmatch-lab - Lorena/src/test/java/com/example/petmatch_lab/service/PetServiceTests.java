package com.example.petmatch_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petmatch_lab.dto.PetForm;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.repository.PetRepository;

@ExtendWith(MockitoExtension.class)
class PetServiceTests {

    @Mock
    private PetRepository repository;

    private PetService service;

    @BeforeEach
    void setUp() {
        service = new PetService(repository);
    }

    @Test
    void shouldNormalizeFieldsBeforeSaving() {
        PetForm form = newForm("  Kira  ", "  Dog  ", 4);
        form.setDescription("  Very playful.  ");
        when(repository.save(any(Pet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Pet created = service.create(form);

        assertEquals("Kira", created.getName());
        assertEquals("Dog", created.getSpecies());
        assertEquals("Very playful.", created.getDescription());
        verify(repository).save(any(Pet.class));
    }

    @Test
    void shouldRejectInvalidAgeBeforeCallingRepository() {
        PetForm form = newForm("Kira", "Dog", -1);

        assertThrows(IllegalArgumentException.class, () -> service.create(form));

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingPet() {
        PetForm form = newForm("Kira", "Dog", 5);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(service.update(999L, form).isEmpty());
        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void shouldUpdateExistingPetWithoutCreatingAnotherOne() {
        PetForm form = newForm("Milo", "Cat", 5);
        Pet existing = new Pet("Milo", "Cat", 2, null);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Pet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Pet updated = service.update(7L, form).orElseThrow();

        assertEquals(5, updated.getAge());
        verify(repository).save(existing);
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingPet() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertFalse(service.delete(999L));
        verify(repository, never()).delete(any(Pet.class));
    }

    @Test
    void shouldDeleteExistingPet() {
        Pet existing = new Pet("Luna", "Dog", 3, null);
        when(repository.findById(8L)).thenReturn(Optional.of(existing));

        assertTrue(service.delete(8L));
        verify(repository).delete(existing);
    }

    @Test
    void shouldFindBySpeciesUsingTrimmedValue() {
        Pet dog = new Pet("Ana", "Dog", 4, null);
        when(repository.findBySpeciesIgnoreCaseOrderByNameAsc("Dog"))
            .thenReturn(List.of(dog));

        List<Pet> result = service.findBySpecies("  Dog  ");

        assertEquals(1, result.size());
        verify(repository).findBySpeciesIgnoreCaseOrderByNameAsc("Dog");
    }

    @Test
    void shouldRejectBlankSpeciesWhenSearching() {
        assertThrows(IllegalArgumentException.class, () -> service.findBySpecies("   "));
    }

    private PetForm newForm(String name, String species, Integer age) {
        PetForm form = new PetForm();
        form.setName(name);
        form.setSpecies(species);
        form.setAge(age);
        return form;
    }
}
