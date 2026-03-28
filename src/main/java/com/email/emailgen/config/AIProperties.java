package com.email.emailgen.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.ai")
public class AIProperties {

    private Retry retry = new Retry();
    private Prompt prompt = new Prompt();

    @Min(1)
    private int maxPromptLength = 12000;
    @Getter
    @Setter
    public static class Retry {
        @Min(0)
        private int maxAttempts = 2;

        private Duration backoff = Duration.ofMillis(400);
    }
    @Getter
    @Setter
    public static class Prompt {
        @NotBlank
        private String defaultTone = "professional";
    }
}
