package com.example.petmatch_lab.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PetRequest(
    @NotBlank(message = "A pet name is required")
    @Size(max = 100, message = "The name must have at most 100 characters")
    String name,

    @NotBlank(message = "A species is required")
    @Size(max = 80, message = "The species must have at most 80 characters")
    String species,

    @NotNull(message = "An age is required")
    @Min(value = 0, message = "The age cannot be negative")
    Integer age,

    @Size(max = 1000,
          message = "The description must have at most 1000 characters")
    String description
) {
}
