package com.email.emailgen.controller;

import com.email.emailgen.dto.AIResponse;
import com.email.emailgen.dto.EmailRequest;
import com.email.emailgen.service.EmailPromptFactory;
import com.email.emailgen.service.orchestrator.AIOrchestrator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final AIOrchestrator orchestrator;
    private final EmailPromptFactory promptFactory;

    public EmailController(AIOrchestrator orchestrator, 
                           EmailPromptFactory promptFactory) {
        this.orchestrator = orchestrator;
        this.promptFactory = promptFactory;
    }

    @PostMapping("/generate")
    public AIResponse generate(@Valid @RequestBody EmailRequest request) {
        validateRequest(request);
        String prompt = promptFactory.buildPrompt(request);
        return orchestrator.generate(prompt);
    }

    private void validateRequest(EmailRequest request) {
        String mode = request
                .getMode() == null ? "reply" : request.getMode().trim().toLowerCase();
        String emailContent = request
                .getEmailContent() == null ? "" : request.getEmailContent().trim();
        String instruction = request
                .getUserInstruction() == null ? "" : request.getUserInstruction().trim();

        if ("compose".equals(mode) && instruction.isBlank()) {
            throw new IllegalArgumentException("Compose mode requires a user instruction.");
        }

        if ("reply".equals(mode) && emailContent.isBlank()) {
            throw new IllegalArgumentException("Reply mode requires email content.");
        }

        if (emailContent.isBlank() && instruction.isBlank()) {
            throw new IllegalArgumentException("Provide email content or a user instruction.");
        }
    }
}
