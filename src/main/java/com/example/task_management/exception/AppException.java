package com.example.task_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public AppException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public static AppException userNotFound(Long userId) {
        return new AppException("User with ID " + userId + " not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public static AppException taskNotFound(Long taskId) {
        return new AppException("Task with ID " + taskId + " not found", "TASK_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public static AppException invalidRequest(String message) {
        return new AppException(message, "INVALID_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public static AppException emailAlreadyExists(String email) {
        return new AppException("Email " + email + " is already in use", "EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }

    public static AppException invalidEmailFormat(String email) {
        return new AppException("Email " + email + " has an invalid format", "INVALID_EMAIL_FORMAT", HttpStatus.BAD_REQUEST);
    }
}
