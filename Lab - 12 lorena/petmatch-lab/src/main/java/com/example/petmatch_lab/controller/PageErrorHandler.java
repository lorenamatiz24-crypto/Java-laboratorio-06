package com.example.petmatch_lab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.petmatch_lab.exception.BusinessRuleException;
import com.example.petmatch_lab.exception.ResourceNotFoundException;

@ControllerAdvice(annotations = Controller.class)
public class PageErrorHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(
        ResourceNotFoundException exception,
        Model model
    ) {
        model.addAttribute("errorTitle", "Recurso no encontrado");
        model.addAttribute("errorMessage", exception.getMessage());
        return "errors/status";
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(
        BusinessRuleException exception,
        Model model
    ) {
        model.addAttribute("errorTitle", "Operación no permitida");
        model.addAttribute("errorMessage", exception.getMessage());
        return "errors/status";
    }
}
