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
         task.setDescription(taskDetails.getDescription());
         task.setStatus(taskDetails.getStatus());
         return taskRepository.save(task);
     }
     @DeleteMapping("/tasks/{id}")
     public ResponseEntity<?> deleteTask(@PathVariable Long id) {
         TaskEntity task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
         taskRepository.delete(task);
         return ResponseEntity.ok().build();
     }
}
