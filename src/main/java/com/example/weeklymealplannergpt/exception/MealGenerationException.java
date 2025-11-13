package com.example.weeklymealplannergpt.exception;

public class MealGenerationException extends RuntimeException {
    public MealGenerationException(String message) {
        super(message);
    }
    
    public MealGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
