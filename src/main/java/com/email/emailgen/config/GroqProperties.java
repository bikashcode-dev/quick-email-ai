package com.email.emailgen.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "groq")
public class GroqProperties {
    @NotBlank
    private String apiUrl;
    @NotBlank
    private String apiKey;
    @NotBlank
    private String chatEndpoint;
    @NotBlank
    private String model;
    private Duration timeout = Duration.ofSeconds(12);
}
