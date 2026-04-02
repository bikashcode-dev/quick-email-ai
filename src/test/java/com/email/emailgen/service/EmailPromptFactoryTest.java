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

    @Test
    void shouldBuildComposePromptWithComposeOnlyGuardrails() {
        AIProperties properties = new AIProperties();
        properties.getPrompt().setDefaultTone("professional");

        EmailPromptFactory factory = new EmailPromptFactory(properties);
        EmailRequest request = new EmailRequest();
        request.setMode("compose");
        request.setUserInstruction("boss ko mail likho kal mai absent rahunga");

        String prompt = factory.buildPrompt(request);

        assertTrue(prompt.contains("This is compose mode for a brand-new outgoing email, not a reply."));
        assertTrue(prompt.contains("Automatically correct grammar, spelling, and wording mistakes before drafting the email."));
        assertTrue(prompt.contains("Never generate a reply from the recipient's side"));
        assertTrue(prompt.contains("Detect who the email is for and choose a suitable greeting automatically."));
    }
}
