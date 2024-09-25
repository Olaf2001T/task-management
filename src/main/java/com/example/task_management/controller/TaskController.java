package com.example.task_management.controller;

import com.example.task_management.model.Task;
import com.example.task_management.model.TaskStatus;
import com.example.task_management.model.UserDto;
import com.example.task_management.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {

        return taskService.findAllTasks().stream()
                .filter(task -> (status == null || task.getStatus() == status))
                .filter(task -> (title == null || task.getTitle().contains(title)))
                .filter(task -> (dueDate == null || task.getDueDate().isEqual(dueDate)))
                .collect(Collectors.toList());
    }




    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.findTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> addTask(@Valid @RequestBody Task task) {
        return ResponseEntity.ok(taskService.addTask(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id, @Valid @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Task> patchTask(
            @PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.patchTask(id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/assign-users")
    public ResponseEntity<Task> assignUsersToTask(
            @PathVariable Long id, @RequestBody Set<Long> userIds) {
        Task task = taskService.assignUsersToTask(id, userIds);
        return ResponseEntity.ok(task);
    }



    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long id, @RequestBody TaskStatus status) {
        Task updatedTask = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsersByTask(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskTitle) {

        List<Task> tasks = taskService.findAllTasks().stream()
                .filter(task -> {
                    boolean matchesId = (taskId == null || task.getId().equals(taskId));
                    boolean matchesTitle = (taskTitle == null || task.getTitle().equalsIgnoreCase(taskTitle));
                    return matchesId && matchesTitle;
                })
                .toList();

        List<UserDto> usersDto = tasks.stream()
                .flatMap(task -> task.getAssignedUsers().stream())
                .distinct()
                .map(user -> new UserDto(user.getId(), user.getFirstName(), user.getLastName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(usersDto);
    }

}
