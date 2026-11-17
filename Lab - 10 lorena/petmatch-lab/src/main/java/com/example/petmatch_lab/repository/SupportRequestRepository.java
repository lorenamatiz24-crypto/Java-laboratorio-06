package com.example.petmatch_lab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;

public interface SupportRequestRepository
    extends JpaRepository<SupportRequest, Long> {

    List<SupportRequest> findByPet_IdOrderByIdDesc(Long petId);

    List<SupportRequest> findByPet_IdAndStatusOrderByIdDesc(
        Long petId,
        RequestStatus status
    );

    Optional<SupportRequest> findByIdAndPet_Id(Long id, Long petId);

    boolean existsByPet_Id(Long petId);

    boolean existsByPet_IdAndStatus(Long petId, RequestStatus status);
}
