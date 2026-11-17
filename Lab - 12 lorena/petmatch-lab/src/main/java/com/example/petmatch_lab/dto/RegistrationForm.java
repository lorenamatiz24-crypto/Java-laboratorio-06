package com.example.petmatch_lab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "A name is required")
    @Size(max = 100, message = "The name must have at most 100 characters")
    private String displayName;

    @NotBlank(message = "An email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 190, message = "The email must have at most 190 characters")
    private String email;

    @NotBlank(message = "A password is required")
    @Size(min = 12, max = 64,
          message = "The password must have between 12 and 64 characters")
    private String password;

    @NotBlank(message = "Confirm the password")
    private String confirmPassword;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
