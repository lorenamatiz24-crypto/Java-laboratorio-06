package com.example.petmatch_lab.api.dto;

public record PetResponse(
    Long id,
    String name,
    String species,
    int age,
    String description
) {
}
