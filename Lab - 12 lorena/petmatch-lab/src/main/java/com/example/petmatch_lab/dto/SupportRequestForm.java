package com.example.petmatch_lab.dto;

import com.example.petmatch_lab.model.SupportType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SupportRequestForm {

    @NotNull(message = "Selecciona el tipo de apoyo")
    private SupportType type;

    @NotBlank(message = "Escribe una descripción")
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String description;

    public SupportType getType() {
        return type;
    }

    public void setType(SupportType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
