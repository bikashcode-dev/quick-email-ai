package com.email.emailgen.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration apiMapping = registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");

        CorsRegistration authMapping = registry.addMapping("/auth/**")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");

        if (!corsProperties.getAllowedOrigins().isEmpty()) {
            String[] allowedOrigins = corsProperties.getAllowedOrigins().toArray(String[]::new);
            apiMapping.allowedOrigins(allowedOrigins);
            authMapping.allowedOrigins(allowedOrigins);
        }

        if (!corsProperties.getAllowedOriginPatterns().isEmpty()) {
            String[] allowedOriginPatterns = corsProperties.getAllowedOriginPatterns().toArray(String[]::new);
            apiMapping.allowedOriginPatterns(allowedOriginPatterns);
            authMapping.allowedOriginPatterns(allowedOriginPatterns);
        }
    }
}
