package com.example.task_management.service;

import com.example.task_management.exception.AppException;
import com.example.task_management.model.TaskDao;
import com.example.task_management.model.User;
import com.example.task_management.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User addUser(User user) {
        validateEmail(user.getEmail());
        checkIfEmailExists(user.getEmail());
        return userRepository.save(user);
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));
    }

    public User updateUser(Long userId, User updatedUser) {
        User user = findUserById(userId);

        if (!user.getEmail().equalsIgnoreCase(updatedUser.getEmail())) {
            checkIfEmailExists(updatedUser.getEmail());
        }

        validateEmail(updatedUser.getEmail());
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());

        return userRepository.save(user);
    }

    private void validateEmail(String email) {
        if (!emailPattern.matcher(email).matches()) {
            throw AppException.invalidEmailFormat(email);
        }
    }

    private void checkIfEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw AppException.emailAlreadyExists(email);
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));

        user.getTasks().forEach(task -> task.getAssignedUsers().remove(user));

        userRepository.deleteById(userId);
    }

    public User patchUser(Long userId, User partialUpdate) {
        User user = findUserById(userId);

        if (StringUtils.hasText(partialUpdate.getFirstName())) {
            user.setFirstName(partialUpdate.getFirstName());
        }

        if (StringUtils.hasText(partialUpdate.getLastName())) {
            user.setLastName(partialUpdate.getLastName());
        }

        if (StringUtils.hasText(partialUpdate.getEmail())) {
            if (!user.getEmail().equalsIgnoreCase(partialUpdate.getEmail())) {
                checkIfEmailExists(partialUpdate.getEmail());
            }
            validateEmail(partialUpdate.getEmail());
            user.setEmail(partialUpdate.getEmail());
        }

        return userRepository.save(user);
    }

    public List<TaskDao> findTasksByUser(Long userId, String lastName, String email) {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> (userId == null || user.getId().equals(userId)))
                .filter(user -> (lastName == null || user.getLastName().equalsIgnoreCase(lastName)))
                .filter(user -> (email == null || user.getEmail().equalsIgnoreCase(email)))
                .toList();

        if (users.isEmpty()) {
            throw new AppException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        return users.stream()
                .flatMap(user -> user.getTasks().stream())
                .map(task -> new TaskDao(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getDueDate(),
                        task.getStatus()))
                .collect(Collectors.toList());
    }

}
