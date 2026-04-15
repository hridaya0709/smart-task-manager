package com.ai_practice.github.smart_task_manager.service;

import com.ai_practice.github.smart_task_manager.entity.TaskEntity;

public interface TaskAIPriorityService {
    String prioritize(TaskEntity task);
}
