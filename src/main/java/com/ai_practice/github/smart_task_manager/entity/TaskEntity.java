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
}