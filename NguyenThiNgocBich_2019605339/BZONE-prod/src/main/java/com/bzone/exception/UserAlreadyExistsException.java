package com.bzone.exception;

public class UserAlreadyExistsException extends RuntimeException  {
    private String message;

    public UserAlreadyExistsException() {
    }
    public UserAlreadyExistsException(String s) {
        super(s);
        this.message = s;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
