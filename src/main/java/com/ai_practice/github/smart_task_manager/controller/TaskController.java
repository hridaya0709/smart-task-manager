package com.ai_practice.github.smart_task_manager.controller;

// import the dependencies needed to create a controller class
import com.ai_practice.github.smart_task_manager.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.RestController;

// import all the missing dependencies for this controller class
import com.ai_practice.github.smart_task_manager.entity.TaskEntity;
import com.ai_practice.github.smart_task_manager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

// import @RequestMapping of springframework to map the base URL for the controller
import org.springframework.web.bind.annotation.RequestMapping;

// create the TaskController class that will handle the HTTP requests for the Task entity
@RestController
@RequestMapping("/smart-task-manager/api")
public class TaskController {
     @Autowired
     private TaskRepository taskRepository;

     @GetMapping("/tasks")
     public List<TaskEntity> getAllTasks() {
         return taskRepository.findAll();
     }

     @PostMapping("/tasks")
     public TaskEntity createTask(@RequestBody TaskEntity task) {
         if (task.getDescription() == null || task.getDescription().isEmpty()) {
             task.setDescription(generateDescription(task.getTitle()));
         }
         return taskRepository.save(task);
     }

     @GetMapping("/tasks/{id}")
     public TaskEntity getTaskById(@PathVariable Long id) {
         return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
     }

     @PutMapping("/tasks/{id}")
     public TaskEntity updateTask(@PathVariable Long id, @RequestBody TaskEntity taskDetails) {
         TaskEntity task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
         task.setTitle(taskDetails.getTitle());
         // If description is not provided, make use of generateDescription method
         // to generate a description based on the title
         if (taskDetails.getDescription() == null || taskDetails.getDescription().isEmpty()) {
             task.setDescription(generateDescription(taskDetails.getTitle()));
         } else {
                task.setDescription(taskDetails.getDescription());
         }
         task.setStatus(taskDetails.getStatus());
         return taskRepository.save(task);
     }

     @DeleteMapping("/tasks/{id}")
     public ResponseEntity<?> deleteTask(@PathVariable Long id) {
         TaskEntity task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
         taskRepository.delete(task);
         return ResponseEntity.ok().build();
     }

     // create a generateDescription method that will generate a description for a task based
    // on its title using few-shot prompting
     private String generateDescription(String title) {

         // Few-shot prompting: generate a description for the task based on the title
         // if the description is not provided.
         // You can use the following examples as a reference:
         // when creating a new task, if the description is not provided, generate a description based on below examples
         // example 1: if the title is "Buy groceries", the description can be
         // "Buy milk, eggs, bread and fruits from the supermarket"
         // example 2: if the title is "Finish project report", the description can be
         // "Complete the final report for the project and submit it to the manager by the end of the week"
         // example 3: if the title is "Plan weekend trip", the description can be
         // "Research and book a hotel, plan the itinerary and pack the bags for the weekend trip"

         String lowerTitle = title.toLowerCase();
         if (lowerTitle.contains("buy groceries")) {
             return "Buy milk, eggs, bread and fruits from the supermarket";
         } else if (lowerTitle.contains("finish project report")) {
             return "Complete the final report for the project and submit it to the manager by the end of the week";
         } else if (lowerTitle.contains("plan weekend trip")) {
             return "Research and book a hotel, plan the itinerary and pack the bags for the weekend trip";
         } else {
             return "No description provided";
         }
     }
}
