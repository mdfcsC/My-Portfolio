package edu.uob.exception;

public class MyError extends RuntimeException {
    // This identifier helps with object serialization compatibility across different versions
    private static final long serialVersionUID = 20250312L;

    private String message;

    public MyError(String message) {
        this.message = message;
    }
    @Override
    public String getMessage() {
        return message;
    }
}
