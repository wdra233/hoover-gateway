package com.eric.projects.hoover.exceptions;

public class HooverException extends RuntimeException {

    public HooverException(String message) {
        super(message);
    }

    public HooverException(String message, Throwable cause) {
        super(message, cause);
    }
}
