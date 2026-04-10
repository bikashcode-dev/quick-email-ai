package com.email.emailgen.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "resend")
public class ResendProperties {

    private String apiUrl = "https://api.resend.com";
    private String apiKey;
    private String fromEmail = "onboarding@resend.dev";
    private Duration timeout = Duration.ofSeconds(15);

    public boolean canSend() {
        return apiKey != null && !apiKey.isBlank();
    }
}
