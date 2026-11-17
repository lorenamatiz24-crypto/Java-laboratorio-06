package com.example.petmatch_lab.model;

import com.example.petmatch_lab.exception.BusinessRuleException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "support_requests")
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    protected SupportRequest() {
    }

    public SupportRequest(Pet pet, SupportType type, String description) {
        if (pet == null || type == null
            || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Invalid support request data");
        }

        this.pet = pet;
        this.type = type;
        this.description = description.strip();
        this.status = RequestStatus.OPEN;
    }

    public void complete() {
        ensureOpen();
        status = RequestStatus.COMPLETED;
    }

    public void cancel() {
        ensureOpen();
        status = RequestStatus.CANCELLED;
    }

    private void ensureOpen() {
        if (status != RequestStatus.OPEN) {
            throw new BusinessRuleException(
                "La solicitud no está abierta y no puede cambiar de estado."
            );
        }
    }

    public boolean isOpen() {
        return status == RequestStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public Pet getPet() {
        return pet;
    }

    public SupportType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public RequestStatus getStatus() {
        return status;
    }
}
