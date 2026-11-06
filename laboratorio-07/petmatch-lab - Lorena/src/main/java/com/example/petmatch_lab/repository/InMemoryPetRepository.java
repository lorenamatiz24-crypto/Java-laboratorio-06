package com.example.petmatch_lab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.petmatch_lab.model.Pet;

@Repository
public class InMemoryPetRepository {

    private final List<Pet> pets = List.of(
        new Pet(1L, "Luna", "Perro", 3, "Mediano", "Disfruta los paseos y los juegos."),
        new Pet(2L, "Milo", "Gato", 2, "Pequeño", "Prefiere lugares tranquilos."),
        new Pet(3L, "Nube", "Conejo", 1, "Pequeño", "Es curioso y le gusta explorar."),
        new Pet(4L, "Bebe", "Hámster", 4, "Pequeño", "Es juguetón y le gusta correr."),
        new Pet(5L, "Toby", "Tortuga", 8, "Mediano", "Es tranquilo y disfruta tomar el sol.")
    );

    public List<Pet> findAll() {
        return pets;
    }

    public Optional<Pet> findById(long id) {
        for (Pet pet : pets) {
            if (pet.getId() == id) {
                return Optional.of(pet);
            }
        }

        return Optional.empty();
    }
}
