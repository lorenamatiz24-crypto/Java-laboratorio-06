package com.example.petmatch_lab.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.petmatch_lab.dto.PetForm;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.repository.InMemoryPetRepository;

@Service
public class PetService {

    private final InMemoryPetRepository petRepository;

    public PetService(InMemoryPetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    public Optional<Pet> findById(long id) {
        return petRepository.findById(id);
    }

    public Pet create(PetForm form) {
        validateForm(form);

        return petRepository.create(
            form.getName().strip(),
            form.getSpecies().strip(),
            form.getAge(),
            normalizeDescription(form.getDescription())
        );
    }

    public Optional<Pet> update(long id, PetForm form) {
        validateForm(form);

        return petRepository.update(
            id,
            form.getName().strip(),
            form.getSpecies().strip(),
            form.getAge(),
            normalizeDescription(form.getDescription())
        );
    }

    public boolean delete(long id) {
        return petRepository.delete(id);
    }

    private void validateForm(PetForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Pet form is required");
        }
        if (invalidRequiredText(form.getName(), 100)) {
            throw new IllegalArgumentException("Pet name is invalid");
        }
        if (invalidRequiredText(form.getSpecies(), 80)) {
            throw new IllegalArgumentException("Pet species is invalid");
        }
        if (form.getAge() == null || form.getAge() < 0) {
            throw new IllegalArgumentException("Pet age is invalid");
        }
        if (form.getDescription() != null
            && form.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Pet description is too long");
        }
    }

    private boolean invalidRequiredText(String value, int maxLength) {
        return value == null
            || value.strip().isEmpty()
            || value.strip().length() > maxLength;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.strip();
    }
}
