package com.ai_practice.github.smart_task_manager.entity;

// import the dependencies needed to create an entity class
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
// import @Getter of lombok to generate getter methods for the fields
import lombok.Getter;
import lombok.NoArgsConstructor;
// import @Setter of lombok to generate setter methods for the fields
import lombok.Setter;

import java.time.LocalDateTime;

// create the Task entity class with id, title, description and status fields
@Entity
@Data
// Use @Getter and @Setter annotations to generate getter and setter methods for the fields
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String status;

    // Add two fields for createdAt and updatedAt to track when the task was created and last updated
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Add two fields for priority and dueDate to allow users to set the priority and due date for the task
    private String priority;
    private String dueDate;

    // Add category field to allow users to categorize their tasks
    private String category;
}