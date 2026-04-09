package com.ai_practice.github.smart_task_manager.service;

import com.ai_practice.github.smart_task_manager.exception.AiServiceTimeoutException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Set;

@Service
public class OllamaTaskCategoryService implements TaskAIService {

    private static final Logger log = LoggerFactory.getLogger(OllamaTaskCategoryService.class);

    private static final Set<String> CATEGORIES = Set.of(
            "Work", "Personal", "Health", "Finance", "Education",
            "Shopping", "Travel", "Home", "General"
    );

    private final RestClient restClient;
    private final String model;

    public OllamaTaskCategoryService(
            @Value("${ollama.url}") String ollamaUrl,
            @Value("${ollama.model}") String model,
            @Value("${ollama.connect-timeout-seconds}") int connectTimeoutSeconds,
            @Value("${ollama.read-timeout-seconds}") int readTimeoutSeconds
    ) {
        this.model = model;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        this.restClient = RestClient.builder()
                .baseUrl(ollamaUrl)
                .requestFactory(requestFactory)
                .build();
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

        String prompt = buildPrompt(text);

        try {
            OllamaGenerateRequest genRequest = new OllamaGenerateRequest(model, prompt, false);
            OllamaGenerateResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(genRequest)
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            return extractCategory(response);
        }  catch (RestClientException ex) {
            throw new AiServiceTimeoutException("Ollama request timed out", ex);
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

    private String extractCategory(OllamaGenerateResponse response) {
        if (response == null || response.response() == null) {
            return "General";
        }

        String responseText = response.response().trim().split("\n")[0];

        for (String category : CATEGORIES) {
            if (responseText.toLowerCase().contains(category.toLowerCase())) {
                return category;
            }
        }

        return "General";
    }

    private record OllamaGenerateRequest(
            String model,
            String prompt,
            boolean stream
    ) {}

    private record OllamaGenerateResponse(
            String model,
            String response,
            boolean done,
            @JsonProperty("created_at") String createdAt,
            @JsonProperty("total_duration") long totalDuration,
            @JsonProperty("load_duration") long loadDuration,
            @JsonProperty("prompt_eval_count") int promptEvalCount,
            @JsonProperty("prompt_eval_duration") long promptEvalDuration,
            @JsonProperty("eval_count") int evalCount,
            @JsonProperty("eval_duration") long evalDuration
    ) {}
}
