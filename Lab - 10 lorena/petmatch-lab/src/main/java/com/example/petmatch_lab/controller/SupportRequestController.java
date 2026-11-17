package com.example.petmatch_lab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petmatch_lab.dto.SupportRequestForm;
import com.example.petmatch_lab.exception.BusinessRuleException;
import com.example.petmatch_lab.exception.ResourceNotFoundException;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.SupportType;
import com.example.petmatch_lab.service.PetService;
import com.example.petmatch_lab.service.SupportRequestService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/pets/{petId}/requests")
public class SupportRequestController {

    private final PetService petService;
    private final SupportRequestService requestService;

    public SupportRequestController(
        PetService petService,
        SupportRequestService requestService
    ) {
        this.petService = petService;
        this.requestService = requestService;
    }

    @GetMapping
    public String list(@PathVariable("petId") long petId, Model model) {
        model.addAttribute("pet", findPet(petId));
        model.addAttribute("requests", requestService.findAllForPet(petId));
        return "requests/list";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable("petId") long petId, Model model) {
        model.addAttribute("supportRequestForm", new SupportRequestForm());
        return prepareForm(model, petId);
    }

    @PostMapping
    public String create(
        @PathVariable("petId") long petId,
        @Valid @ModelAttribute("supportRequestForm") SupportRequestForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        findPet(petId);

        if (bindingResult.hasErrors()) {
            return prepareForm(model, petId);
        }

        try {
            requestService.create(petId, form);
        } catch (BusinessRuleException exception) {
            bindingResult.reject("request.rule", exception.getMessage());
            return prepareForm(model, petId);
        }

        redirectAttributes.addFlashAttribute(
            "successMessage", "Solicitud registrada correctamente."
        );
        return "redirect:/pets/" + petId + "/requests";
    }

    @PostMapping("/{requestId}/complete")
    public String complete(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId,
        RedirectAttributes redirectAttributes
    ) {
        requestService.complete(petId, requestId);
        redirectAttributes.addFlashAttribute(
            "successMessage", "Solicitud completada correctamente."
        );
        return "redirect:/pets/" + petId + "/requests";
    }

    @PostMapping("/{requestId}/cancel")
    public String cancel(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId,
        RedirectAttributes redirectAttributes
    ) {
        requestService.cancel(petId, requestId);
        redirectAttributes.addFlashAttribute(
            "successMessage", "Solicitud cancelada correctamente."
        );
        return "redirect:/pets/" + petId + "/requests";
    }

    @GetMapping("/{requestId}/replace")
    public String replaceForm(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId,
        Model model
    ) {
        var request = requestService.findOneForPet(petId, requestId);

        if (!request.isOpen()) {
            throw new BusinessRuleException(
                "Solo se puede reemplazar una solicitud abierta."
            );
        }

        SupportRequestForm form = new SupportRequestForm();
        form.setType(request.getType());
        form.setDescription(request.getDescription());
        model.addAttribute("supportRequestForm", form);

        return prepareReplacementForm(model, petId, requestId);
    }

    @PostMapping("/{requestId}/replace")
    public String replace(
        @PathVariable("petId") long petId,
        @PathVariable("requestId") long requestId,
        @Valid @ModelAttribute("supportRequestForm") SupportRequestForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var request = requestService.findOneForPet(petId, requestId);
            if (!request.isOpen()) {
                throw new BusinessRuleException(
                    "Solo se puede reemplazar una solicitud abierta."
                );
            }
            return prepareReplacementForm(model, petId, requestId);
        }

        requestService.replace(petId, requestId, form);

        redirectAttributes.addFlashAttribute(
            "successMessage", "La solicitud fue reemplazada correctamente."
        );
        return "redirect:/pets/" + petId + "/requests";
    }

    private String prepareForm(Model model, long petId) {
        model.addAttribute("pet", findPet(petId));
        model.addAttribute("supportTypes", SupportType.values());
        model.addAttribute("formAction", "/pets/" + petId + "/requests");
        model.addAttribute("formTitle", "Nueva solicitud de apoyo");
        model.addAttribute("submitLabel", "Registrar solicitud");
        return "requests/form";
    }

    private String prepareReplacementForm(
        Model model,
        long petId,
        long requestId
    ) {
        prepareForm(model, petId);
        model.addAttribute(
            "formAction",
            "/pets/" + petId + "/requests/" + requestId + "/replace"
        );
        model.addAttribute("formTitle", "Reemplazar solicitud abierta");
        model.addAttribute("submitLabel", "Reemplazar solicitud");
        return "requests/form";
    }

    private Pet findPet(long petId) {
        return petService.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe la mascota indicada."
            ));
    }
}
