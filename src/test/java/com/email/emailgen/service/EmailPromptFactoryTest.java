package com.email.emailgen.service;

import com.email.emailgen.config.AIProperties;
import com.email.emailgen.dto.EmailRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(prompt.contains("- Do not include a subject line."));
    }

    @Test
    void shouldAllowFullEmailFormatWhenUserExplicitlyAsksForIt() {
        AIProperties properties = new AIProperties();
        properties.getPrompt().setDefaultTone("professional");

        EmailPromptFactory factory = new EmailPromptFactory(properties);
        EmailRequest request = new EmailRequest();
        request.setMode("compose");
        request.setUserInstruction("HR ko 5 din ki chutti ke liye short email likho, subject, greeting, message aur closing include karo");

        String prompt = factory.buildPrompt(request);

        assertTrue(prompt.contains("- Include a clear subject line at the top."));
        assertTrue(prompt.contains("- Include a suitable greeting, message body, and professional closing."));
        assertFalse(prompt.contains("- Do not include a subject line."));
    }

    @Test
    void shouldBuildReplySummaryWhenSummaryToneIsSelected() {
        AIProperties properties = new AIProperties();
        properties.getPrompt().setDefaultTone("professional");

        EmailPromptFactory factory = new EmailPromptFactory(properties);
        EmailRequest request = new EmailRequest();
        request.setMode("reply");
        request.setTone("summarize");
        request.setEmailContent("Please review the attached proposal and share your approval by Friday.");

        String prompt = factory.buildPrompt(request);

        assertTrue(prompt.contains("Summarize the email instead of drafting a reply."));
        assertTrue(prompt.contains("- Detected intent: email summary"));
        assertTrue(prompt.contains("- Response length: short"));
    }

    @Test
    void shouldDetectShortLengthForComposeInstruction() {
        AIProperties properties = new AIProperties();
        properties.getPrompt().setDefaultTone("professional");

        EmailPromptFactory factory = new EmailPromptFactory(properties);
        EmailRequest request = new EmailRequest();
        request.setMode("compose");
        request.setUserInstruction("manager ko short leave request email likho");

        String prompt = factory.buildPrompt(request);

        assertTrue(prompt.contains("- Detected intent: leave request"));
        assertTrue(prompt.contains("- Response length: short"));
    }

    @Test
    void shouldDiscouragePlaceholdersForSimpleLeaveEmails() {
        AIProperties properties = new AIProperties();
        properties.getPrompt().setDefaultTone("professional");

        EmailPromptFactory factory = new EmailPromptFactory(properties);
        EmailRequest request = new EmailRequest();
        request.setMode("compose");
        request.setUserInstruction("email liko for leave ke liye 5 din ki reason fever");

        String prompt = factory.buildPrompt(request);

        assertTrue(prompt.contains("Do not insert bracket placeholders like \"[start date]\", \"[end date]\", \"[Your Name]\", or similar"));
        assertTrue(prompt.contains("Prefer 4 to 6 sentences at most unless the user asks for a detailed email."));
        assertTrue(prompt.contains("Wrong style: add placeholders like \"[start date]\" or a long generic paragraph."));
    }
}
