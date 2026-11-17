package com.example.petmatch_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.petmatch_lab.dto.PetForm;
import com.example.petmatch_lab.repository.InMemoryPetRepository;

class PetServiceTests {

    private PetService service;

    @BeforeEach
    void setUp() {
        service = new PetService(new InMemoryPetRepository());
    }

    @Test
    void shouldReturnAvailablePets() {
        assertEquals(3, service.findAll().size());
    }

    @Test
    void shouldReturnEmptyWhenPetDoesNotExist() {
        assertTrue(service.findById(999L).isEmpty());
    }

    @Test
    void shouldNormalizeNameAndSpeciesWhenCreatingPet() {
        PetForm form = newForm("  Kira  ", "  Perro  ", 4);
        form.setDescription("  Juguetona  ");

        var created = service.create(form);

        assertEquals("Kira", created.getName());
        assertEquals("Perro", created.getSpecies());
        assertEquals("Juguetona", created.getDescription());
    }

    @Test
    void shouldRejectNegativeAgeWhenServiceIsCalledDirectly() {
        PetForm form = newForm("Kira", "Perro", -1);

        assertThrows(IllegalArgumentException.class, () -> service.create(form));
        assertEquals(3, service.findAll().size());
    }

    @Test
    void shouldRejectBlankNameWhenServiceIsCalledDirectly() {
        PetForm form = newForm("   ", "Perro", 2);

        assertThrows(IllegalArgumentException.class, () -> service.create(form));
    }

    @Test
    void shouldUpdateExistingPet() {
        PetForm form = newForm("Milo", "Gato", 5);

        var updated = service.update(2L, form);

        assertTrue(updated.isPresent());
        assertEquals(5, service.findById(2L).orElseThrow().getAge());
    }

    @Test
    void shouldNotCreatePetWhenUpdateTargetDoesNotExist() {
        PetForm form = newForm("Desconocido", "Gato", 5);

        assertTrue(service.update(999L, form).isEmpty());
        assertEquals(3, service.findAll().size());
    }

    private PetForm newForm(String name, String species, Integer age) {
        PetForm form = new PetForm();
        form.setName(name);
        form.setSpecies(species);
        form.setAge(age);
        return form;
    }
}
