package com.example.petmatch_lab.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;

import jakarta.persistence.EntityManager;

@DataJpaTest
class SupportRequestRepositoryTests {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SupportRequestRepository requestRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistRequestWithExistingPet() {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Luna", "Dog", 3, null)
        );
        SupportRequest saved = requestRepository.saveAndFlush(
            new SupportRequest(pet, SupportType.WALK, "Needs a walk")
        );
        Long requestId = saved.getId();
        Long petId = pet.getId();

        entityManager.clear();
        SupportRequest found = requestRepository.findById(requestId)
            .orElseThrow();

        assertNotNull(found.getId());
        assertEquals(petId, found.getPet().getId());
        assertEquals(RequestStatus.OPEN, found.getStatus());
    }

    @Test
    void shouldFindRequestsOnlyForSelectedPet() {
        Pet first = petRepository.saveAndFlush(
            new Pet("Luna", "Dog", 3, null)
        );
        Pet second = petRepository.saveAndFlush(
            new Pet("Milo", "Cat", 2, null)
        );

        requestRepository.saveAndFlush(
            new SupportRequest(first, SupportType.WALK, "Walk")
        );
        requestRepository.saveAndFlush(
            new SupportRequest(second, SupportType.COMPANY, "Company")
        );

        List<SupportRequest> found =
            requestRepository.findByPet_IdOrderByIdDesc(first.getId());

        assertEquals(1, found.size());
        assertEquals(SupportType.WALK, found.get(0).getType());
        assertTrue(requestRepository.existsByPet_Id(first.getId()));
        assertFalse(requestRepository.existsByPet_Id(Long.MAX_VALUE));
    }

    @Test
    void shouldNotFindRequestUnderAnotherPet() {
        Pet first = petRepository.saveAndFlush(
            new Pet("Luna", "Dog", 3, null)
        );
        Pet second = petRepository.saveAndFlush(
            new Pet("Milo", "Cat", 2, null)
        );
        SupportRequest saved = requestRepository.saveAndFlush(
            new SupportRequest(first, SupportType.FEEDING, "Feed")
        );

        assertTrue(requestRepository.findByIdAndPet_Id(
            saved.getId(), second.getId()
        ).isEmpty());
    }
}
