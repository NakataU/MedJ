package com.medj.exception;

import org.springframework.http.HttpStatus;

public class MedJFileAlreadyExists extends BaseMedJException {

    private static final String CODE = "FILE_ALREADY_EXISTS";

    public MedJFileAlreadyExists() {
        super("File already exists", CODE, HttpStatus.CONFLICT.value());
    }

    public MedJFileAlreadyExists(String message) {
        super(message, CODE, HttpStatus.CONFLICT.value());
    }

    public MedJFileAlreadyExists(String message, Throwable cause) {
        super(message, cause, CODE, HttpStatus.CONFLICT.value());
    }
}