package com.email.emailgen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmailRequest {
    @Size(max = 12000, message = "Email content must be 12000 characters or fewer")
    private String emailContent;

    @Size(max = 400, message = "Tone must be 400 characters or fewer")
    @Pattern(
            regexp = "^[a-zA-Z\\s-]*$",
            message = "Tone can only contain letters, spaces, and hyphens"
    )
    private String tone;

    @Size(max = 12000, message = "Previous reply must be 12000 characters or fewer")
    private String previousReply;

    @Size(max = 4000, message = "User instruction must be 4000 characters or fewer")
    private String userInstruction;

    @Pattern(
            regexp = "^(reply|compose)?$",
            message = "Mode must be either reply or compose"
    )
    private String mode;

    @Max(value = 10, message = "Variation index must be 10 or fewer")
    private Integer variationIndex;

}
