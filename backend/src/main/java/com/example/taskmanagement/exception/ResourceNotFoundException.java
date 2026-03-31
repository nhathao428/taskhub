package com.example.taskmanagement.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found: id=" + id);
    }

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(resourceName + " not found: " + field + "=" + value);
    }
}
