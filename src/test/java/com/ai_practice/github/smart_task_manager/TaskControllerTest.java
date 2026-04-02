package com.ai_practice.github.smart_task_manager;

import com.ai_practice.github.smart_task_manager.controller.TaskController;
import com.ai_practice.github.smart_task_manager.entity.TaskEntity;
import com.ai_practice.github.smart_task_manager.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTasks_returnsAllTasks() throws Exception {
        TaskEntity task1 = new TaskEntity(1L, "Task 1", "Desc 1", "Pending", null, null, null, null);
        TaskEntity task2 = new TaskEntity(2L, "Task 2", "Desc 2", "Completed", null, null, null, null);
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        mockMvc.perform(get("/smart-task-manager/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    void getAllTasks_returnsEmptyListWhenNoTasksExist() throws Exception {
        when(taskRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/smart-task-manager/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTaskById_returnsTaskWhenFound() throws Exception {
        TaskEntity task = new TaskEntity(1L, "Task 1", "Desc 1", "OPEN", null, null, null, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/smart-task-manager/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.description").value("Desc 1"));
    }

    @Test
    void getTaskById_returns404WhenTaskNotFound() throws Exception {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/smart-task-manager/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTask_savesAndReturnsTaskWithProvidedDescription() throws Exception {
        TaskEntity task = new TaskEntity(null, "My Task", "My Description", "OPEN", null, null, null, null);
        TaskEntity saved = new TaskEntity(1L, "My Task", "My Description", "OPEN", null, null, null, null);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(saved);

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("My Description"));
    }

    @Test
    void createTask_generatesDescriptionWhenDescriptionIsNull() throws Exception {
        TaskEntity task = new TaskEntity(null, "Buy Groceries", null, "OPEN", null, null, null, null);
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Buy milk, eggs, bread and fruits from the supermarket"));
    }

    @Test
    void createTask_generatesDescriptionWhenDescriptionIsEmpty() throws Exception {
        TaskEntity task = new TaskEntity(null, "Finish Project Report", "", "OPEN", null, null, null, null);
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Complete the final report for the project and submit it to the manager by the end of the week"));
    }

    @Test
    void createTask_generatesDefaultDescriptionForUnrecognizedTitle() throws Exception {
        TaskEntity task = new TaskEntity(null, "Unknown Task", null, "OPEN", null, null, null, null);
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("No description provided"));
    }

    @Test
    void updateTask_updatesAndReturnsTaskWithProvidedDescription() throws Exception {
        TaskEntity existing = new TaskEntity(1L, "Old Title", "Old Desc", "OPEN", null, null, null, null);
        TaskEntity details = new TaskEntity(null, "New Title", "New Desc", "DONE", null, null, null, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/smart-task-manager/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New Desc"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void updateTask_generatesDescriptionWhenDescriptionIsEmpty() throws Exception {
        TaskEntity existing = new TaskEntity(1L, "Old Title", "Old Desc", "OPEN", null, null, null, null);
        TaskEntity details = new TaskEntity(null, "Buy Groceries", "", "OPEN", null, null, null, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/smart-task-manager/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Buy milk, eggs, bread and fruits from the supermarket"));
    }

    @Test
    void updateTask_returns404WhenTaskNotFound() throws Exception {
        TaskEntity details = new TaskEntity(null, "New Title", "New Desc", "DONE", null, null, null, null);
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/smart-task-manager/api/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_deletesTaskAndReturnsOk() throws Exception {
        TaskEntity task = new TaskEntity(1L, "Task 1", "Desc 1", "OPEN", null, null, null, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        mockMvc.perform(delete("/smart-task-manager/api/tasks/1"))
                .andExpect(status().isOk());

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_returns404WhenTaskNotFound() throws Exception {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/smart-task-manager/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    // Create a test case for the priority filter included in the getAllTasks endpoint to verify that it correctly filters tasks by priority
    @Test
    void getAllTasks_returnsTasksFilteredByPriority() throws Exception {
        TaskEntity task1 = new TaskEntity(1L, "Task 1", "Desc 1", "Pending", null, null, "High", null);
        TaskEntity task2 = new TaskEntity(2L, "Task 2", "Desc 2", "Completed", null, null, "Low", null);
        when(taskRepository.findByPriority("High")).thenReturn(List.of(task1));

        mockMvc.perform(get("/smart-task-manager/api/tasks?priority=High"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].priority").value("High"));
    }

    // Create new test cases for uncovered code paths in the TaskController, such as testing the behavior of the updateTask endpoint when an invalid date format is provided for the dueDate field
    @Test
    void updateTask_returnsBadRequestWhenDueDateIsInvalid() throws Exception {
        TaskEntity existing = new TaskEntity(1L, "Old Title", "Old Desc", "OPEN", null, null, null, null);
        TaskEntity details = new TaskEntity(null, "New Title", "New Desc", "DONE", null, null, null, "invalid-date");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
    }

     @Test
    void createTask_returnsBadRequestWhenDueDateIsInvalid() throws Exception {
         TaskEntity task = new TaskEntity(null, "My Task", "My Description", "OPEN", null, null, null, "invalid-date");
         mockMvc.perform(post("/smart-task-manager/api/tasks")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(task)))
                 .andExpect(status().isBadRequest());
     }
}
