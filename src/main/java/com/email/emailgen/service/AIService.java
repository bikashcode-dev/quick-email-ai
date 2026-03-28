package com.email.emailgen.service;

import com.email.emailgen.dto.AIResponse;

public interface AIService {
    AIResponse generate(String prompt);
}