package com.example.petmatch_lab.api.dto;

import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportType;

public record SupportRequestResponse(
    Long id,
    Long petId,
    SupportType type,
    String description,
    RequestStatus status
) {
}
