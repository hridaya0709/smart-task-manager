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

// import TaskAIService
import com.ai_practice.github.smart_task_manager.service.TaskAIService;

@WebMvcTest(controllers = TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Create a mock bean for TaskAIService to allow the TaskController to be tested without requiring an actual implementation of the service
    @MockBean
    private TaskAIService taskAIService;

    @Test
    void getAllTasks_returnsAllTasks() throws Exception {
        // create task1 and task2 using task() helper method to set up the tasks with default values, then override the id, title, description and status fields to match the id, title, description and status used in the test
        TaskEntity task1Entity = task(t -> {
            t.setId(1L);
            t.setTitle("Task 1");
            t.setDescription("Desc 1");
            t.setStatus("Pending");
        });
        TaskEntity task2Entity = task(t -> {
            t.setId(2L);
            t.setTitle("Task 2");
            t.setDescription("Desc 2");
            t.setStatus("Completed");
        });
        when(taskRepository.findAll()).thenReturn(List.of(task1Entity, task2Entity));

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
        // create task using task() helper method to set up the task with default values, then override the id, title and description fields to match the id, title and description used in the test
         TaskEntity task = task(t -> {
             t.setId(1L);
             t.setTitle("Task 1");
             t.setDescription("Desc 1");
         });
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
        TaskEntity task;
        TaskEntity saved;
        // create task and saved using task() helper method to set up the task and saved with default values, then override the fields as needed for the test
         task = task(t -> {
             t.setTitle("My Task");
             t.setDescription("My Description");
         });
         saved = task(t -> {
             t.setId(1L);
             t.setTitle("My Task");
             t.setDescription("My Description");
         });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
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
        TaskEntity task;
        // create task using task() helper method to set up the task with default values, then override the title and description fields to match the title and description used in the test
         task = task(t -> {
             t.setTitle("Buy Groceries");
             t.setDescription(null);
         });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Buy milk, eggs, bread and fruits from the supermarket"));
    }

    @Test
    void createTask_generatesDescriptionWhenDescriptionIsEmpty() throws Exception {
         TaskEntity task;
        // create task using task() helper method to set up the task with default values, then override the title and description fields to match the title and description used in the test
         task = task(t -> {
             t.setTitle("Finish Project Report");
             t.setDescription("");
         });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Complete the final report for the project and submit it to the manager by the end of the week"));
    }

    @Test
    void createTask_generatesDefaultDescriptionForUnrecognizedTitle() throws Exception {
        // create task using task() helper method to set up the task with default values, then override the title field to match the title used in the test
        TaskEntity task = task(t -> t.setTitle("Unknown Task"));
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("No description provided"));
    }

    @Test
    void updateTask_updatesAndReturnsTaskWithProvidedDescription() throws Exception {
         TaskEntity existing;
         TaskEntity details;
        // Create existing and details tasks using task() helper method to set up the existing task and details with default values, then override the fields as needed for the test
         existing = task(t -> {
             t.setId(1L);
             t.setTitle("Old Title");
             t.setDescription("Old Desc");
             t.setStatus("OPEN");
         });
         details = task(t -> {
             t.setTitle("New Title");
             t.setDescription("New Desc");
             t.setStatus("DONE");
         });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
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

        // Create existing and details tasks using task() helper method to set up the existing task and details with default values, then override the fields as needed for the test
         TaskEntity existing = task(t -> {
             t.setId(1L);
             t.setTitle("Old Title");
             t.setDescription("Old Desc");
             t.setStatus("OPEN");
         });
         TaskEntity details = task(t -> {
             t.setTitle("Buy Groceries");
             t.setDescription("");
             t.setStatus("OPEN");
         });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
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
        // Create details task using task() helper method to set up the details with default values, then override the title field to match the title used in the test
        TaskEntity details = task(t -> t.setTitle("Buy Groceries"));
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/smart-task-manager/api/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_deletesTaskAndReturnsOk() throws Exception {
        // Modify the task creation using task() helper method to set up the existing task with default values, then override the id field to match the id used in the test
        TaskEntity task = task(t -> t.setId(1L));
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        mockMvc.perform(delete("/smart-task-manager/api/tasks/1"))
                .andExpect(status().isOk());

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_returns404WhenTaskNotFound() throws Exception {
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/smart-task-manager/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    // Create a test case for the priority filter included in the getAllTasks endpoint to verify that it correctly filters tasks by priority
    @Test
    void getAllTasks_returnsTasksFilteredByPriority() throws Exception {
        // Modify the creation of task1 and task2 using task() method
        TaskEntity task1 = task(t -> {
            t.setId(1L);
            t.setTitle("Task 1");
            t.setPriority("High");
        });
        TaskEntity task2 = task(t -> {
            t.setId(2L);
            t.setTitle("Task 2");
            t.setPriority("Low");
        });
        TaskEntity task3 = task(t -> {
            t.setId(3L);
            t.setTitle("Task 3");
            t.setPriority("High");
        });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.findByPriority("High")).thenReturn(List.of(task1, task3));

        mockMvc.perform(get("/smart-task-manager/api/tasks?priority=High"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].priority").value("High"));
    }

    // Create new test cases for uncovered code paths in the TaskController, such as testing the behavior of the updateTask endpoint when an invalid date format is provided for the dueDate field
   @Test
    void updateTask_returnsBadRequestWhenDueDateIsInvalid() throws Exception {
        TaskEntity existing = task(t -> {
            t.setId(1L);
            t.setTitle("Old Title");
            t.setDescription("Old Desc");
            t.setStatus("OPEN");
            t.setDueDate("2026-01-01");
        });

        TaskEntity details = task(t -> {
            t.setTitle("New Title");
            t.setDescription("New Desc");
            t.setStatus("DONE");
            t.setDueDate("invalid-date");
        });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        mockMvc.perform(put("/smart-task-manager/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isBadRequest());
    }

     @Test
    void createTask_returnsBadRequestWhenDueDateIsInvalid() throws Exception {
        // Create a task using task() helper method
            TaskEntity task = task(t -> {
                t.setTitle("New Task");
                t.setDescription("New Desc");
                t.setStatus("OPEN");
                t.setDueDate("invalid-date");
            });
         when(taskAIService.categorize(any(), any())).thenReturn("General");
        mockMvc.perform(post("/smart-task-manager/api/tasks")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(task)))
                 .andExpect(status().isBadRequest());
     }

     // Create test cases for the uncovered code paths in the TaskController related to the category field, such as testing the behavior of the createTask and updateTask endpoints when a category is provided in the request body
    @Test
    void createTask_savesAndReturnsTaskWithProvidedCategory() throws Exception {
        TaskEntity task = task(t -> {
            t.setTitle("New Task");
            t.setDescription("New Desc");
            t.setCategory("Work");
        });
        TaskEntity saved = task(t -> {
            t.setId(1L);
            t.setTitle("New Task");
            t.setDescription("New Desc");
            t.setCategory("Work");
        });
        when(taskAIService.categorize(any(), any())).thenReturn("Work");
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(saved);

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("Work"));
    }

    @Test
    void updateTask_updatesAndReturnsTaskWithProvidedCategory() throws Exception {
        TaskEntity existing = task(t -> {
            t.setId(1L);
            t.setTitle("Old Title");
            t.setDescription("Old Desc");
            t.setCategory("Personal");
        });
        TaskEntity details = task(t -> {
            t.setTitle("New Title");
            t.setDescription("New Desc");
            t.setCategory("Work");
        });
        when(taskAIService.categorize(any(), any())).thenReturn("Work");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/smart-task-manager/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New Desc"))
                .andExpect(jsonPath("$.category").value("Work"));
    }

    // Create test cases for priority field in createTask and updateTask endpoints
    @Test
    void createTask_savesAndReturnsTaskWithProvidedPriority() throws Exception {
        TaskEntity task = task(t -> {
            t.setTitle("New Task");
            t.setDescription("New Desc");
            t.setPriority("High");
        });
        TaskEntity saved = task(t -> {
            t.setId(1L);
            t.setTitle("New Task");
            t.setDescription("New Desc");
            t.setPriority("High");
        });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(saved);

        mockMvc.perform(post("/smart-task-manager/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.priority").value("High"));
    }

    @Test
    void updateTask_updatesAndReturnsTaskWithProvidedPriority() throws Exception {
        TaskEntity existing = task(t -> {
            t.setId(1L);
            t.setTitle("Old Title");
            t.setDescription("Old Desc");
            t.setPriority("Low");
        });
        TaskEntity details = task(t -> {
            t.setTitle("New Title");
            t.setDescription("New Desc");
            t.setPriority("High");
        });
        when(taskAIService.categorize(any(), any())).thenReturn("General");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/smart-task-manager/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New Desc"))
                .andExpect(jsonPath("$.priority").value("High"));
    }

    private TaskEntity task(java.util.function.Consumer<TaskEntity> overrides) {
        TaskEntity t = new TaskEntity();
        t.setTitle(null);
        t.setDescription(null);
        t.setStatus(null);
        t.setCreatedAt(null);
        t.setUpdatedAt(null);
        t.setPriority(null);
        t.setDueDate(null);
        t.setCategory(null);
        overrides.accept(t);
        return t;
    }
}
