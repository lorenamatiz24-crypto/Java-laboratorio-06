package com.example.petmatch_lab.api.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.petmatch_lab.api.dto.SupportRequestRequest;
import com.example.petmatch_lab.api.dto.SupportRequestResponse;
import com.example.petmatch_lab.api.mapper.SupportRequestApiMapper;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.service.SupportRequestService;

@RestController
@RequestMapping(
    path = "/api/v1/pets/{petId}/requests",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class SupportRequestRestController {

    private final SupportRequestService requestService;
    private final SupportRequestApiMapper mapper;

    public SupportRequestRestController(
        SupportRequestService requestService,
        SupportRequestApiMapper mapper
    ) {
        this.requestService = requestService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<SupportRequestResponse> list(
        @PathVariable("petId") long petId
    ) {
        return requestService.findAllForPet(petId).stream()
            .map(request -> mapper.toResponse(request, petId))
            .toList();
    }

    @GetMapping("/{requestId}")
    public SupportRequestResponse detail(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId
    ) {
        SupportRequest request = requestService.findOneForPet(
            petId, requestId
        );
        return mapper.toResponse(request, petId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SupportRequestResponse> create(
        @PathVariable("petId") long petId,
        @Valid @RequestBody SupportRequestRequest body
    ) {
        SupportRequest created = requestService.create(
            petId, mapper.toForm(body)
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{requestId}")
            .buildAndExpand(created.getId())
            .toUri();

        return ResponseEntity.created(location)
            .body(mapper.toResponse(created, petId));
    }

    @PostMapping("/{requestId}/complete")
    public ResponseEntity<Void> complete(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId
    ) {
        requestService.complete(petId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancel(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId
    ) {
        requestService.cancel(petId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(
        path = "/{requestId}/replace",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SupportRequestResponse> replace(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId,
        @Valid @RequestBody SupportRequestRequest body
    ) {
        SupportRequest replacement = requestService.replace(
            petId, requestId, mapper.toForm(body)
        );

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/pets/{petId}/requests/{requestId}")
            .buildAndExpand(petId, replacement.getId())
            .toUri();

        return ResponseEntity.created(location)
            .body(mapper.toResponse(replacement, petId));
    }
}
