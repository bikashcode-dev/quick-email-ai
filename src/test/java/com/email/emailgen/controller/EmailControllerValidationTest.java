package com.email.emailgen.controller;

import com.email.emailgen.exception.GlobalExceptionHandler;
import com.email.emailgen.service.EmailPromptFactory;
import com.email.emailgen.service.orchestrator.AIOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailControllerValidationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AIOrchestrator orchestrator = Mockito.mock(AIOrchestrator.class);
        EmailPromptFactory promptFactory = Mockito.mock(EmailPromptFactory.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new EmailController(orchestrator, promptFactory))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldRejectInvalidToneCharacters() throws Exception {
        String payload = """
                {
                  "emailContent": "Need status update",
                  "tone": "urgent!!!"
                }
                """;

        mockMvc.perform(post("/api/email/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.details[0]").value("tone: Tone can only contain letters, spaces, and hyphens"));
    }
}
