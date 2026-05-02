package com.medj.exception;

public class BaseMedJException extends RuntimeException {

    private final String code;
    private final int status;

    public BaseMedJException(String message, String code, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public BaseMedJException(String message, Throwable cause, String code, int status) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
