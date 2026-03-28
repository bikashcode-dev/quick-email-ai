package com.email.emailgen.service.impl;

import com.email.emailgen.config.AIProperties;
import com.email.emailgen.config.GroqProperties;
import com.email.emailgen.dto.AIResponse;
import com.email.emailgen.exception.AIClientException;
import com.email.emailgen.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("groqService")
public class GroqServiceImpl implements AIService {

    private final WebClient webClient;
    private final GroqProperties groqProperties;
    private final AIProperties aiProperties;
    private final ObjectMapper mapper = new ObjectMapper();

    public GroqServiceImpl(WebClient.Builder builder,
                           GroqProperties groqProperties,
                           AIProperties aiProperties) {
        this.webClient = builder.baseUrl(groqProperties.getApiUrl()).build();
        this.groqProperties = groqProperties;
        this.aiProperties = aiProperties;
    }

    @Override
    public AIResponse generate(String prompt) {
        try {
            Map<String, Object> request = Map.of(
                    "model", groqProperties.getModel(),
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );
            String response = webClient.post()
                    .uri(groqProperties.getChatEndpoint())
                    .header("Authorization", "Bearer " + groqProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.createException().flatMap(ex -> {
                                log.warn("Groq request failed with status {}", ex.getStatusCode().value());
                                return Mono.error(toClientException(ex));
                            }))
                    .bodyToMono(String.class)
                    .timeout(groqProperties.getTimeout())
                    .retryWhen(Retry.backoff(aiProperties.getRetry().getMaxAttempts(), aiProperties.getRetry().getBackoff())
                            .filter(this::isRetryable))
                    .block();

            JsonNode root = mapper.readTree(response);
            String text = root.at("/choices/0/message/content").asText();
            JsonNode usage = root.path("usage");

            return new AIResponse(
                    text,
                    usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").asInt(),
                    "GROQ"
            );

        } catch (AIClientException ex) {
            throw ex;
        } catch (WebClientResponseException ex) {
            throw toClientException(ex);
        } catch (IOException ex) {
            log.error("Groq response parsing failed: {}", ex.getClass().getSimpleName());
            throw new AIClientException("GROQ", "Groq returned an unreadable response.", false, ex);
        } catch (Exception e) {
            log.error("Groq failed: {}", e.getClass().getSimpleName());
            throw new AIClientException("GROQ", "Groq request failed.", isRetryable(e), e);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof AIClientException ex) {
            return ex.isRetryable();
        }
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
        }
        return true;
    }

    private AIClientException toClientException(WebClientResponseException ex) {
        boolean retryable = ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
        return new AIClientException("GROQ",
                "Groq request failed with status " + ex.getStatusCode().value() + ".",
                retryable,
                ex);
    }
}
