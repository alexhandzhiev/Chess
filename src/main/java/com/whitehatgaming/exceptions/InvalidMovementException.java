package com.whitehatgaming.exceptions;

public class InvalidMovementException extends Exception {

    public InvalidMovementException() {
        System.out.println("Invalid movement!");
    }

    public InvalidMovementException(String message) {
        System.out.println(message);
    }
}
