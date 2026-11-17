package com.example.petmatch_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petmatch_lab.dto.SupportRequestForm;
import com.example.petmatch_lab.exception.BusinessRuleException;
import com.example.petmatch_lab.exception.ResourceNotFoundException;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@ExtendWith(MockitoExtension.class)
class SupportRequestServiceTests {

    @Mock
    private PetRepository petRepository;

    @Mock
    private SupportRequestRepository requestRepository;

    private SupportRequestService service;

    @BeforeEach
    void setUp() {
        service = new SupportRequestService(petRepository, requestRepository);
    }

    @Test
    void shouldRejectSecondOpenRequest() {
        Pet pet = new Pet("Luna", "Dog", 3, null);
        when(petRepository.findById(7L)).thenReturn(Optional.of(pet));
        when(requestRepository.existsByPet_IdAndStatus(
            7L, RequestStatus.OPEN
        )).thenReturn(true);

        assertThrows(
            BusinessRuleException.class,
            () -> service.create(7L, validForm())
        );

        verify(requestRepository, never()).save(any(SupportRequest.class));
    }

    @Test
    void shouldRejectMissingPetBeforeSaving() {
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> service.create(999L, validForm())
        );

        verify(requestRepository, never()).save(any(SupportRequest.class));
    }

    @Test
    void shouldRejectCompletingRequestOwnedByAnotherPet() {
        when(requestRepository.findByIdAndPet_Id(15L, 7L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> service.complete(7L, 15L)
        );
    }

    @Test
    void shouldCancelOpenRequest() {
        Pet pet = new Pet("Luna", "Dog", 3, null);
        SupportRequest request = new SupportRequest(
            pet, SupportType.WALK, "Needs a walk"
        );
        when(requestRepository.findByIdAndPet_Id(15L, 7L))
            .thenReturn(Optional.of(request));

        service.cancel(7L, 15L);

        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }

    @Test
    void shouldRejectInvalidFormBeforeAccessingRepositories() {
        SupportRequestForm form = validForm();
        form.setDescription("   ");

        assertThrows(
            IllegalArgumentException.class,
            () -> service.create(7L, form)
        );

        verify(petRepository, never()).findById(7L);
    }

    private SupportRequestForm validForm() {
        SupportRequestForm form = new SupportRequestForm();
        form.setType(SupportType.WALK);
        form.setDescription("Needs a walk");
        return form;
    }
}
