package com.email.emailgen.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountStatusRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;
}
