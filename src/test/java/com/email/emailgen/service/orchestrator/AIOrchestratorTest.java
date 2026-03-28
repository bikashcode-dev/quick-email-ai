package com.email.emailgen.service.orchestrator;

import com.email.emailgen.dto.AIResponse;
import com.email.emailgen.exception.AIClientException;
import com.email.emailgen.exception.AllProvidersFailedException;
import com.email.emailgen.service.AIService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AIOrchestratorTest {

    @Test
    void shouldFallbackToGeminiWhenGroqFails() {
        AIService groq = mock(AIService.class);
        AIService gemini = mock(AIService.class);
        AIResponse expected = new AIResponse("reply", 1, 1, 2, "GEMINI");

        when(groq.generate("prompt")).thenThrow(new AIClientException("GROQ", "timeout", true, null));
        when(gemini.generate("prompt")).thenReturn(expected);

        AIOrchestrator orchestrator = new AIOrchestrator(groq, gemini);

        AIResponse result = orchestrator.generate("prompt");

        assertEquals("GEMINI", result.getProvider());
    }

    @Test
    void shouldThrowWhenAllProvidersFail() {
        AIService groq = mock(AIService.class);
        AIService gemini = mock(AIService.class);

        when(groq.generate("prompt")).thenThrow(new AIClientException("GROQ", "timeout", true, null));
        when(gemini.generate("prompt")).thenThrow(new AIClientException("GEMINI", "bad gateway", true, null));

        AIOrchestrator orchestrator = new AIOrchestrator(groq, gemini);

        assertThrows(AllProvidersFailedException.class, () -> orchestrator.generate("prompt"));
    }
}
