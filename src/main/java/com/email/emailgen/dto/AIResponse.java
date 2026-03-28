package com.email.emailgen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIResponse {
    private String text;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private String provider;
}