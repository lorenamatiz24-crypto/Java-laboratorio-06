
package com.example.petmatch_lab.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.petmatch_lab.model.Pet;


@Repository
public class InMemoryPetRepository {

    private final Map<Long, Pet> pets = new LinkedHashMap<>();
    private long nextId = 4L;

    public InMemoryPetRepository() {
        pets.put(1L, new Pet(1L, "Luna", "Perro", 3,"Disfruta los paseos y los juegos."));
        pets.put(2L, new Pet(2L, "Milo", "Gato", 2, "Prefiere lugares tranquilos."));
        pets.put(3L, new Pet(3L, "Nube", "Conejo", 1, "Es curioso y le gusta explorar."));
    }

    public synchronized List<Pet> findAll() {
        return List.copyOf(pets.values());
    }

    public synchronized Optional<Pet> findById(long id) {
        return Optional.ofNullable(pets.get(id));
    }

    public synchronized Pet create(
        String name,
        String species,
        int age,
        String description
    ) {
        long id = nextId++;
        Pet pet = new Pet(id, name, species, age, description);
        pets.put(id, pet);
        return pet;
    }

    public synchronized Optional<Pet> update(
        long id,
        String name,
        String species,
        int age,
        String description
    ) {
        if (!pets.containsKey(id)) {
            return Optional.empty();
        }

        Pet updated = new Pet(id, name, species, age, description);
        pets.put(id, updated);
        return Optional.of(updated);
    }

    public synchronized boolean delete(long id) {
        return pets.remove(id) != null;
    }
}
