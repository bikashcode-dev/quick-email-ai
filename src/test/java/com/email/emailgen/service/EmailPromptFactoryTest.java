package com.email.emailgen.service;

import com.email.emailgen.config.AIProperties;
import com.email.emailgen.dto.EmailRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailPromptFactoryTest {

    @Test
    void shouldUseDefaultToneAndIncludeVariationContext() {
        AIProperties properties = new AIProperties();
        properties.getPrompt().setDefaultTone("professional");

        EmailPromptFactory factory = new EmailPromptFactory(properties);
        EmailRequest request = new EmailRequest();
        request.setEmailContent("Please share the project timeline.");
        request.setVariationIndex(2);
        request.setPreviousReply("Thanks, I will get back to you.");

        String prompt = factory.buildPrompt(request);

        assertTrue(prompt.contains("- Tone: professional"));
        assertTrue(prompt.contains("This is attempt #2."));
        assertTrue(prompt.contains("Previous reply:"));
        assertTrue(prompt.contains("Please share the project timeline."));
    }
}
