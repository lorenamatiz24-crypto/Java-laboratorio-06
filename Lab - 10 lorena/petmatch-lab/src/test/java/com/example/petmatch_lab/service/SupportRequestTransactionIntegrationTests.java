package com.example.petmatch_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.petmatch_lab.dto.SupportRequestForm;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@SpringBootTest
class SupportRequestTransactionIntegrationTests {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SupportRequestRepository requestRepository;

    @Autowired
    private SupportRequestService requestService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldReplaceRequestWithTwoConsistentChanges() {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Luna", "Dog", 3, null)
        );
        SupportRequest original = requestRepository.saveAndFlush(
            new SupportRequest(pet, SupportType.WALK, "Original")
        );

        SupportRequestForm form = new SupportRequestForm();
        form.setType(SupportType.FEEDING);
        form.setDescription("Replacement");

        SupportRequest replacement = requestService.replace(
            pet.getId(), original.getId(), form
        );

        assertNotEquals(original.getId(), replacement.getId());
        assertEquals(RequestStatus.CANCELLED,
            requestRepository.findById(original.getId())
                .orElseThrow().getStatus());
        assertEquals(RequestStatus.OPEN,
            requestRepository.findById(replacement.getId())
                .orElseThrow().getStatus());
    }

    @Test
    void shouldRollbackWhenFailureOccursInsideTransaction() {
        Pet pet = petRepository.saveAndFlush(
            new Pet("Milo", "Cat", 2, null)
        );
        SupportRequest original = requestRepository.saveAndFlush(
            new SupportRequest(pet, SupportType.COMPANY, "Original")
        );
        Long requestId = original.getId();
        TransactionTemplate transaction =
            new TransactionTemplate(transactionManager);

        assertThrows(IllegalStateException.class, () ->
            transaction.executeWithoutResult(status -> {
                SupportRequest managed = requestRepository.findById(requestId)
                    .orElseThrow();
                managed.cancel();
                requestRepository.flush();
                throw new IllegalStateException("Simulated failure");
            })
        );

        assertEquals(RequestStatus.OPEN,
            requestRepository.findById(requestId)
                .orElseThrow().getStatus());
    }
}
