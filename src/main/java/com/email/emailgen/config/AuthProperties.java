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
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();
    private Otp otp = new Otp();
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank
        private String secret = "change-this-jwt-secret-to-a-long-random-value";

        @Min(60000)
        private long expiration = 86400000;
    }

    @Getter
    @Setter
    public static class Otp {
        private Duration expiry = Duration.ofMinutes(5);
    }

    @Getter @Setter
    public static class Mail {
        @NotBlank
        private String subject = "Your QuickMail OTP";
        @NotBlank
        private String fromName = "QuickMail";

        private boolean logOnly = false;
    }
}
