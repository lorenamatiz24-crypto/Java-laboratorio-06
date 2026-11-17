package com.example.petmatch_lab.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.petmatch_lab.exception.BusinessRuleException;

class SupportRequestTests {

    @Test
    void shouldBeginInOpenState() {
        SupportRequest request = newRequest();

        assertEquals(RequestStatus.OPEN, request.getStatus());
        assertTrue(request.isOpen());
    }

    @Test
    void shouldCompleteAnOpenRequest() {
        SupportRequest request = newRequest();

        request.complete();

        assertEquals(RequestStatus.COMPLETED, request.getStatus());
    }

    @Test
    void shouldCancelAnOpenRequest() {
        SupportRequest request = newRequest();

        request.cancel();

        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }

    @Test
    void shouldRejectCompletingAnAlreadyCompletedRequest() {
        SupportRequest request = newRequest();
        request.complete();

        assertThrows(BusinessRuleException.class, request::complete);
        assertEquals(RequestStatus.COMPLETED, request.getStatus());
    }

    @Test
    void shouldRejectCancellingACompletedRequest() {
        SupportRequest request = newRequest();
        request.complete();

        assertThrows(BusinessRuleException.class, request::cancel);
        assertEquals(RequestStatus.COMPLETED, request.getStatus());
    }

    @Test
    void shouldRejectCompletingACancelledRequest() {
        SupportRequest request = newRequest();
        request.cancel();

        assertThrows(BusinessRuleException.class, request::complete);
        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }

    private SupportRequest newRequest() {
        Pet pet = new Pet("Luna", "Dog", 3, null);
        return new SupportRequest(pet, SupportType.WALK, "Needs a walk");
    }
}
