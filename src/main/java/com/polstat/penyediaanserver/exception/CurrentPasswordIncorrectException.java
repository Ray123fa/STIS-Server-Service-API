package com.polstat.penyediaanserver.exception;

public class CurrentPasswordIncorrectException extends RuntimeException {
    public CurrentPasswordIncorrectException(String message) {
        super(message);
    }
}