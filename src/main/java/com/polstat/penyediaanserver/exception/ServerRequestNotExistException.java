package com.polstat.penyediaanserver.exception;

public class ServerRequestNotExistException extends RuntimeException {
    public ServerRequestNotExistException(String message) {
        super(message);
    }
}
