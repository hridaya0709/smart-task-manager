package com.ai_practice.github.smart_task_manager.repository;

// import the dependencies needed to create a repository interface
import com.ai_practice.github.smart_task_manager.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// create the TaskRepository interface that extends JpaRepository to provide CRUD operations for TaskEntity
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByPriority(String priority);
}
