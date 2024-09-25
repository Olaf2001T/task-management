package com.example.task_management.controller;

import com.example.task_management.exception.AppException;
import com.example.task_management.model.TaskDao;
import com.example.task_management.model.User;
import com.example.task_management.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email) {

        return userService.findAllUsers().stream()
                .filter(user -> (firstName == null || user.getFirstName().contains(firstName)))
                .filter(user -> (lastName == null || user.getLastName().contains(lastName)))
                .filter(user -> (email == null || user.getEmail().equalsIgnoreCase(email)))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.addUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id, @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUser(
            @PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.patchUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDao>> getTasksByUser(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email) {

        List<User> users = userService.findAllUsers().stream()
                .filter(user -> (userId == null || user.getId().equals(userId)))
                .filter(user -> (lastName == null || user.getLastName().equalsIgnoreCase(lastName)))
                .filter(user -> (email == null || user.getEmail().equalsIgnoreCase(email)))
                .toList();

        List<TaskDao> tasks = users.stream()
                .flatMap(user -> user.getTasks().stream())
                .distinct()
                .map(task -> new TaskDao(task.getId(), task.getTitle(), task.getDescription(), task.getDueDate(), task.getStatus()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tasks);
    }



}
