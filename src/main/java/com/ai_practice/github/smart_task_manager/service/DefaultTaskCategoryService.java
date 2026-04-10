package com.ai_practice.github.smart_task_manager.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

// Fallback service for OllamaTaskCategoryService
@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "false", matchIfMissing = true)
public class DefaultTaskCategoryService implements TaskAIService {
    @Override
    public String categorize(String title, String description) {
        return "General";
    }
}
