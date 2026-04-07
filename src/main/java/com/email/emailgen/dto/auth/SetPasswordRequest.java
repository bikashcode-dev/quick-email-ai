package com.email.emailgen.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetPasswordRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must include at least one letter and one number"
    )
    private String password;
}
