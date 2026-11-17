package com.example.petmatch_lab.api.mapper;

import org.springframework.stereotype.Component;

import com.example.petmatch_lab.api.dto.SupportRequestRequest;
import com.example.petmatch_lab.api.dto.SupportRequestResponse;
import com.example.petmatch_lab.dto.SupportRequestForm;
import com.example.petmatch_lab.model.SupportRequest;

@Component
public class SupportRequestApiMapper {

    public SupportRequestForm toForm(SupportRequestRequest request) {
        SupportRequestForm form = new SupportRequestForm();
        form.setType(request.type());
        form.setDescription(request.description());
        return form;
    }

    public SupportRequestResponse toResponse(
        SupportRequest request,
        long petId
    ) {
        return new SupportRequestResponse(
            request.getId(),
            petId,
            request.getType(),
            request.getDescription(),
            request.getStatus()
        );
    }
}
