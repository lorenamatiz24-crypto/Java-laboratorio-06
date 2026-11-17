package com.example.petmatch_lab.service;

import java.util.Locale;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petmatch_lab.dto.RegistrationForm;
import com.example.petmatch_lab.exception.DuplicateEmailException;
import com.example.petmatch_lab.model.AppUser;
import com.example.petmatch_lab.repository.AppUserRepository;

@Service
public class RegistrationService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
        AppUserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegistrationForm form) {
        validateForm(form);
        String email = normalizeEmail(form.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        AppUser user = new AppUser(
            form.getDisplayName().strip(),
            email,
            passwordEncoder.encode(form.getPassword())
        );

        return userRepository.save(user);
    }

    private void validateForm(RegistrationForm form) {
        if (form == null || invalidText(form.getDisplayName(), 100)
            || invalidText(form.getEmail(), 190)
            || form.getPassword() == null
            || form.getPassword().length() < 12
            || form.getPassword().length() > 64
            || !Objects.equals(
                form.getPassword(), form.getConfirmPassword())) {
            throw new IllegalArgumentException("Invalid registration data");
        }
    }

    private boolean invalidText(String text, int maxLength) {
        return text == null || text.isBlank() || text.strip().length() > maxLength;
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
