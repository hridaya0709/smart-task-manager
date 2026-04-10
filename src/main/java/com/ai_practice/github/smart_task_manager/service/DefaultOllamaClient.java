package com.ai_practice.github.smart_task_manager.service;

import com.ai_practice.github.smart_task_manager.exception.AiServiceTimeoutException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class DefaultOllamaClient implements OllamaClient {

    private final RestClient restClient;
    private final String model;

    // create RestClient once
    public DefaultOllamaClient(
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
    public String generate(String prompt) {
        try{
            OllamaGenerateRequest request = new OllamaGenerateRequest(model, prompt, false);
            OllamaGenerateResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaGenerateResponse.class);
            if(response == null || response.response() == null) {
                return "";
            }
            return response.response().trim();
        } catch(RestClientException ex) {
            throw new AiServiceTimeoutException("Ollama request timed out", ex);
        }
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
