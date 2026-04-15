package com.ai_practice.github.smart_task_manager.service;

import com.ai_practice.github.smart_task_manager.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaTaskPriorityService implements TaskAIPriorityService {

    private static final Logger log = LoggerFactory.getLogger(OllamaTaskPriorityService.class);

    private static final Set<String> PRIORITIES = Set.of(
            "High", "Medium", "Low"
    );

    private final OllamaClient ollamaClient;

    public OllamaTaskPriorityService(DefaultOllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    @Override
    public String prioritize(TaskEntity task) {
        int score = 0;

        // due date urgency
        int dueInDays = calcDueInDays(task.getDueDate());
        score = calcScoreBasedOnDueDate(score, dueInDays);

        String safeTitle = task.getTitle() == null ? "" : task.getTitle().trim();
        String safeDescription = task.getDescription() == null ? "" : task.getDescription().trim();
        String text = (safeTitle + " " + safeDescription).trim();

        score = calcScoreBasedOnText(score, text);

        String status = task.getStatus();
        if ("BLOCKED".equalsIgnoreCase(status)) score += 1;

        String basePriority =
                score >= 5 ? "High" :
                        score >= 2 ? "Medium" : "Low";

        System.out.println("Calculated base priority: " + basePriority + " (score: " + score + ", due in days: " + dueInDays + ")");

        log.debug("Calculated base priority: {} (score: {}, due in days: {})", basePriority, score, dueInDays);

        // Optionally, we could use Ollama to refine the priority based on the text
         String ollamaPrompt = buildPrompt(task, dueInDays);

         try {
             String ollamaResponse = ollamaClient.generate(ollamaPrompt).trim();
             for (String priority : PRIORITIES) {
                 if (ollamaResponse.toLowerCase().contains(priority.toLowerCase())) {
                     return priority;
                 }
             }
         } catch (Exception ex) {
             log.warn("Ollama task prioritization failed, defaulting to calculated priority", ex);
             return basePriority;
         }

        return basePriority;

    }

    private String buildPrompt(TaskEntity task, int dueInDays) {
        return "Given the following task details, determine if the priority should be High, Medium, or Low:\n" +
                "Title: " + task.getTitle() + "\n" +
                "Description: " + task.getDescription() + "\n" +
                "Status: " + task.getStatus() + "\n" +
                "Due in days: " + dueInDays + "\n" +
                "Return only the priority level (High, Medium, Low).";
    }

    private int calcScoreBasedOnText(int score, String text) {
        // text urgency
        String safeText = text == null ? "" : text.toLowerCase();
        if (safeText.contains("urgent") || safeText.contains("asap") || safeText.contains("immediately")) score += 3;
        if (safeText.contains("important") || safeText.contains("soon")) score += 1;

        return score;
    }

    private int calcScoreBasedOnDueDate(int score, int dueInDays) {
        if (dueInDays <= 1) score += 3;
        else if (dueInDays <= 3) score += 2;
        else if (dueInDays <= 7) score += 1;

        return score;
    }

    private int calcDueInDays(String dueDate) {
        if (dueDate == null || dueDate.isBlank()) {
            return Integer.MAX_VALUE; // no due date = lowest urgency
        }

        try {
            LocalDate due = LocalDate.parse(dueDate.trim());
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), due);

            if (days <= 0) {
                return 0; // overdue or due today -> highest urgency bucket
            }

            return days > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) days;
        } catch (java.time.format.DateTimeParseException ex) {
            return Integer.MAX_VALUE; // invalid date format = lowest urgency
        }
    }

}
