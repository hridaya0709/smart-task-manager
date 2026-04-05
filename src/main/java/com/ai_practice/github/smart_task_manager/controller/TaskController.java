package com.ai_practice.github.smart_task_manager.controller;

// import the dependencies needed to create a controller class
import com.ai_practice.github.smart_task_manager.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;

// import all the missing dependencies for this controller class
import com.ai_practice.github.smart_task_manager.entity.TaskEntity;
import com.ai_practice.github.smart_task_manager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

// import @RequestMapping of springframework to map the base URL for the controller
// import the dependencies for Open API documentation
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Add @Tag annotation to group the API endpoints in the Open API documentation
@Tag(name = "Task API", description = "API for managing tasks in the Smart Task Manager application")
// create the TaskController class that will handle the HTTP requests for the Task entity
@RestController
@RequestMapping("/smart-task-manager/api")
public class TaskController {
     @Autowired
     private TaskRepository taskRepository;

     // Add APIOperation annotation to provide a summary and description for the API endpoint in the Open API documentation
    @Operation(summary = "Get all tasks",
            description = "Retrieve a list of all tasks.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tasks returned")
            })
     @GetMapping("/tasks")
     public List<TaskEntity> getAllTasks(@RequestParam(required = false) String priority) {
         if(priority != null && !priority.isBlank()) {
             return taskRepository.findByPriority(priority);
         }
         return taskRepository.findAll();

     }

     // Add APIOperation annotation to provide a summary and description for the API endpoint in the Open API documentation
    @Operation(summary = "Create a new task",
            description = "Create a new task with the provided details. If description is not provided, it will be generated based on the title. If createdAt is not provided, it will be set to the current date. If dueDate is not provided, it will be set to 7 days from the current date.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
     @PostMapping("/tasks")
     public TaskEntity createTask(@RequestBody TaskEntity task) throws BadRequestException {
         if (task.getDescription() == null || task.getDescription().isEmpty() || task.getDescription().isBlank()) {
             if (task.getTitle() == null || task.getTitle().isEmpty() || task.getTitle().isBlank()) {
                 throw new BadRequestException("Title must not be null when description is missing or empty");
             }
             task.setDescription(generateDescription(task.getTitle()));
         }
         // Set createdAt if missing (ISO date: yyyy-MM-dd)
         if (task.getCreatedAt() == null) {
             task.setCreatedAt(LocalDateTime.now());
         }
         // Suggest the category based on the title if they are not provided
         if (task.getCategory() == null || task.getCategory().isEmpty() || task.getCategory().isBlank()) {
             if (task.getTitle() != null && !task.getTitle().isEmpty()) {
                 String lowerTitle = task.getTitle().toLowerCase();
                 if (lowerTitle.contains("groceries") || lowerTitle.contains("shopping")) {
                     task.setCategory("Personal");
                 } else if (lowerTitle.contains("project") || lowerTitle.contains("report")) {
                     task.setCategory("Work");
                 } else if (lowerTitle.contains("trip") || lowerTitle.contains("travel")) {
                     task.setCategory("Leisure");
                 } else {
                     task.setCategory("General");
                 }
             } else {
                 task.setCategory("General");
             }
         }

         // Modify the priority field to suggest a priority level based on the title if it is not provided priority is "Medium". priority can be "Low", "Medium" or "High"
            if (task.getPriority() == null || task.getPriority().isEmpty()) {
                if (task.getTitle() != null && !task.getTitle().isEmpty()) {
                    String lowerTitle = task.getTitle().toLowerCase();
                    if (lowerTitle.contains("urgent") || lowerTitle.contains("asap") || lowerTitle.contains("immediately")) {
                        task.setPriority("High");
                    } else if (lowerTitle.contains("soon") || lowerTitle.contains("important")) {
                        task.setPriority("Medium");
                    } else {
                        task.setPriority("Low");
                    }
                } else {
                    task.setPriority("Medium");
                }
            } else {
                // Validate the provided priority value and set it to "Medium" if it is invalid
                String priority = task.getPriority().trim();
                if (priority.equalsIgnoreCase("Low") || priority.equalsIgnoreCase("Medium") || priority.equalsIgnoreCase("High")) {
                    task.setPriority(priority.substring(0, 1).toUpperCase() + priority.substring(1).toLowerCase());
                } else {
                    task.setPriority("Medium");
                }
            }

         if (task.getDueDate() != null && !task.getDueDate().isBlank()) {
             try {
                 LocalDate due = LocalDate.parse(task.getDueDate().trim());
                 task.setDueDate(due.toString());
             } catch (DateTimeParseException ex) {
                 throw new BadRequestException("Invalid date format for dueDate.");
             }
         } else {
             task.setDueDate(LocalDate.now().plusDays(7).toString());
         }

         return taskRepository.save(task);
     }

     // Add APIOperation annotation to provide a summary and description for the API endpoint in the Open API documentation
    @Operation(summary = "Get a task by ID",
            description = "Retrieve a task by its ID. Returns 404 if the task is not found.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task found and returned"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            })
     @GetMapping("/tasks/{id}")
     public TaskEntity getTaskById(@PathVariable Long id) {
         return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
     }

     // Add APIOperation annotation to provide a summary and description for the API endpoint in the Open API documentation
    @Operation(summary = "Update a task",
            description = "Update an existing task with the provided details. If description is not provided, it will be generated based on the title. If updatedAt is not provided, it will be set to the current date. If dueDate is not provided, it will be set to 7 days from the current date. Returns 404 if the task is not found.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            })
     @PutMapping("/tasks/{id}")
     public TaskEntity updateTask(@PathVariable Long id, @RequestBody TaskEntity taskDetails) throws BadRequestException {
         TaskEntity task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));

         if (taskDetails.getTitle() == null || taskDetails.getTitle().isEmpty() || taskDetails.getTitle().isBlank()) {
                 throw new BadRequestException("Title must not be null when description is missing or empty");
         }
         task.setTitle(taskDetails.getTitle());

         // If description is not provided, make use of generateDescription method
         // to generate a description based on the title
         if (taskDetails.getDescription() == null || taskDetails.getDescription().isEmpty() || taskDetails.getDescription().isBlank()) {
             task.setDescription(generateDescription(taskDetails.getTitle()));
         } else {
             task.setDescription(taskDetails.getDescription());
         }
         task.setStatus(taskDetails.getStatus());

         if (taskDetails.getUpdatedAt() == null) {
             task.setUpdatedAt(LocalDateTime.now());
         }
         else {
             task.setUpdatedAt(taskDetails.getUpdatedAt());
         }

         // Determine the category based on the title if it is not provided
         if (taskDetails.getCategory() == null || taskDetails.getCategory().isEmpty() || taskDetails.getCategory().isBlank()) {
             if (taskDetails.getTitle() != null && !taskDetails.getTitle().isEmpty()) {
                 String lowerTitle = taskDetails.getTitle().toLowerCase();
                 if (lowerTitle.contains("groceries") || lowerTitle.contains("shopping")) {
                     task.setCategory("Personal");
                 } else if (lowerTitle.contains("project") || lowerTitle.contains("report")) {
                     task.setCategory("Work");
                 } else if (lowerTitle.contains("trip") || lowerTitle.contains("travel")) {
                     task.setCategory("Leisure");
                 } else {
                     task.setCategory("General");
                 }
             } else {
                 task.setCategory("General");
             }
         } else {
             task.setCategory(taskDetails.getCategory());
         }

         // Modify the priority field to suggest a priority level based on the title if it is not provided priority is "Medium". priority can be "Low", "Medium" or "High"
         if (taskDetails.getPriority() == null || taskDetails.getPriority().isEmpty()) {
             if (taskDetails.getTitle() != null && !taskDetails.getTitle().isEmpty()) {
                 String lowerTitle = taskDetails.getTitle().toLowerCase();
                 if (lowerTitle.contains("urgent") || lowerTitle.contains("asap") || lowerTitle.contains("immediately")) {
                     task.setPriority("High");
                 } else if (lowerTitle.contains("soon") || lowerTitle.contains("important")) {
                     task.setPriority("Medium");
                 } else {
                     task.setPriority("Low");
                 }
             } else {
                 task.setPriority("Medium");
             }
         } else {
             // Validate the provided priority value and set it to "Medium" if it is invalid
             String priority = taskDetails.getPriority().trim();
             if (priority.equalsIgnoreCase("Low") || priority.equalsIgnoreCase("Medium") || priority.equalsIgnoreCase("High")) {
                 task.setPriority(priority.substring(0, 1).toUpperCase() + priority.substring(1).toLowerCase());
             } else {
                 task.setPriority("Medium");
             }
         }

         if (taskDetails.getDueDate() != null && !taskDetails.getDueDate().isBlank()) {
             try {
                 LocalDate due = LocalDate.parse(taskDetails.getDueDate().trim());
                 task.setDueDate(due.toString());
             } catch (DateTimeParseException ex) {
                 throw new BadRequestException("Invalid date format for dueDate.");
             }
         }

         return taskRepository.save(task);
     }

     // Add APIOperation annotation to provide a summary and description for the API endpoint in the Open API documentation
    @Operation(summary = "Delete a task",
            description = "Delete a task by its ID. Returns 404 if the task is not found.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            })
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
