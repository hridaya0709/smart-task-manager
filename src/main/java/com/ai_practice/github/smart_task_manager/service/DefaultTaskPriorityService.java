package com.ai_practice.github.smart_task_manager.service;

import com.ai_practice.github.smart_task_manager.entity.TaskEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

// Fallback service for OllamaTaskPriorityService
@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "false", matchIfMissing = true)
public class DefaultTaskPriorityService implements TaskAIPriorityService {
    @Override
    public String prioritize(TaskEntity task) {
        return "Medium";
    }
}
