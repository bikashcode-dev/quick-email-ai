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
        CorsRegistration mapping = registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");

        if (!corsProperties.getAllowedOrigins().isEmpty()) {
            mapping.allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new));
        }

        if (!corsProperties.getAllowedOriginPatterns().isEmpty()) {
            mapping.allowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(String[]::new));
        }
    }
}
