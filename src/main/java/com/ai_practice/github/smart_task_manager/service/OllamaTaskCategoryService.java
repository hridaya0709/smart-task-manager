package com.ai_practice.github.smart_task_manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaTaskCategoryService implements TaskAICategoryService {

    private static final Logger log = LoggerFactory.getLogger(OllamaTaskCategoryService.class);

    private static final Set<String> CATEGORIES = Set.of(
            "Work", "Personal", "Health", "Finance", "Education",
            "Shopping", "Travel", "Home", "General"
    );

    private final OllamaClient ollamaClient;

    public OllamaTaskCategoryService(DefaultOllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;

    }

    @Override
    public String categorize(String title, String description) {
        if (title == null && description == null) {
            return "General";
        }

        String safeTitle = title == null ? "" : title.trim();
        String safeDescription = description == null ? "" : description.trim();
        String text = (safeTitle + " " + safeDescription).trim();

        if (text.isBlank()) {
            return "General";
        }

        try {
            String prompt = buildPrompt(text);
            String rawResponse = ollamaClient.generate(prompt);
            return extractCategory(rawResponse);
        } catch (Exception ex) {
            log.warn("Ollama categorization failed, defaulting to General", ex);
            return "General";
        }
    }

    private String buildPrompt(String text) {
        return """
                Classify this task into exactly ONE category from: Work, Personal, Health, Finance, Education, Shopping, Travel, Home, General.
                Task: %s
                Category:""".formatted(text);
    }

    private String extractCategory(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return "General";
        }

        String responseText = rawResponse.trim().split("\n")[0];

        for (String category : CATEGORIES) {
            if (responseText.toLowerCase().contains(category.toLowerCase())) {
                return category;
            }
        }

        return "General";
    }

}
