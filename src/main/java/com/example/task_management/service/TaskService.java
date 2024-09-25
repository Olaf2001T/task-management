package com.example.task_management.service;

import com.example.task_management.exception.AppException;
import com.example.task_management.model.Task;
import com.example.task_management.model.TaskStatus;
import com.example.task_management.model.User;
import com.example.task_management.repository.TaskRepository;
import com.example.task_management.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {
    @PersistenceContext
    private EntityManager entityManager;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public Task addTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task findTaskById(Long taskId) {
        return taskRepository.findByIdWithUsers(taskId)
                .orElseThrow(() -> new AppException("Task not found", "TASK_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    public Task updateTask(Long taskId, Task updatedTask) {
        Task task = findTaskById(taskId);

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(task);
    }

    public Task patchTask(Long taskId, Task partialUpdate) {
        Task task = findTaskById(taskId);

        if (StringUtils.hasText(partialUpdate.getTitle())) {
            task.setTitle(partialUpdate.getTitle());
        }

        if (StringUtils.hasText(partialUpdate.getDescription())) {
            task.setDescription(partialUpdate.getDescription());
        }

        if (partialUpdate.getStatus() != null) {
            task.setStatus(partialUpdate.getStatus());
        }

        if (partialUpdate.getDueDate() != null) {
            task.setDueDate(partialUpdate.getDueDate());
        }

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> AppException.taskNotFound(taskId));

        task.getAssignedUsers().forEach(user -> user.getTasks().remove(task));

        taskRepository.deleteById(taskId);
    }

    @Transactional
    public Task assignUsersToTask(Long taskId, Set<Long> userIds) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException("Task with ID " + taskId + " not found", "TASK_NOT_FOUND", HttpStatus.NOT_FOUND));

        Set<User> users = new HashSet<>(userRepository.findAllById(userIds));

        if (users.size() != userIds.size()) {
            throw new AppException("Some users were not found", "USER_NOT_FOUND", HttpStatus.BAD_REQUEST);
        }

        task.setAssignedUsers(users);
        users.forEach(user -> user.getTasks().add(task));

        taskRepository.save(task);
        taskRepository.flush();
        entityManager.refresh(task);

        return task;
    }

    @Transactional
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(status);

        return taskRepository.save(task);
    }
}
