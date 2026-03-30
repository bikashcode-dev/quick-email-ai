package com.email.emailgen.service.orchestrator;

import com.email.emailgen.dto.AIResponse;
import com.email.emailgen.exception.AIClientException;
import com.email.emailgen.exception.AllProvidersFailedException;
import com.email.emailgen.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AIOrchestrator {

    private final AIService groqAi;
    private final AIService gemini;
    private final AIService openRouter;

    public AIOrchestrator(@Qualifier("groqService") AIService groq,
                          @Qualifier("geminiService") AIService gemini,
                          @Qualifier("openRouterService") AIService openRouter) {
        this.groqAi = groq;
        this.gemini = gemini;
        this.openRouter = openRouter;
    }

    public AIResponse generate(String prompt) {
        try {
            return groqAi.generate(prompt);
        } catch (AIClientException groqError) {
            log.warn("Primary provider {} failed (retryable={}): {}",
                    groqError.getProvider(), groqError.isRetryable(), groqError.getMessage());
        } catch (Exception groqError) {
            log.warn("Primary provider failed unexpectedly: {}", groqError.getClass().getSimpleName());
        }

        try {
            return gemini.generate(prompt);
        } catch (AIClientException geminiError) {
            log.warn("Fallback provider {} failed (retryable={}): {}",
                    geminiError.getProvider(), geminiError.isRetryable(),
                    geminiError.getMessage());
        } catch (Exception geminiError) {
            log.warn("Fallback provider failed unexpectedly: {}", geminiError.getClass().getSimpleName());
        }

        try {
            return openRouter.generate(prompt);
        } catch (AIClientException openRouterError) {
            log.error("Final provider {} failed (retryable={}): {}",
                    openRouterError.getProvider(), openRouterError.isRetryable(), openRouterError.getMessage());
            throw new AllProvidersFailedException("All AI providers failed.", openRouterError);
        } catch (Exception openRouterError) {
            log.error("Final provider failed unexpectedly: {}", openRouterError.getClass().getSimpleName());
            throw new AllProvidersFailedException("All AI providers failed.", openRouterError);
        }
    }
}
