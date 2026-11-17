package com.example.petmatch_lab.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.petmatch_lab.dto.RegistrationForm;
import com.example.petmatch_lab.exception.DuplicateEmailException;
import com.example.petmatch_lab.service.RegistrationService;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("registrationForm") RegistrationForm form,
        BindingResult bindingResult
    ) {
        if (form.getPassword() != null
            && !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue(
                "confirmPassword", "password.mismatch", "Passwords must match"
            );
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            registrationService.register(form);
        } catch (DuplicateEmailException exception) {
            bindingResult.rejectValue("email", "email.duplicate", exception.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }
}
