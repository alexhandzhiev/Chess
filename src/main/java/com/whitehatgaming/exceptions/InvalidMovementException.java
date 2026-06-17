package com.whitehatgaming.exceptions;

public class InvalidMovementException extends Exception {

    public InvalidMovementException() {
        super("Invalid movement!");
    }

    public InvalidMovementException(String message) {
        super(message);
    }
}
