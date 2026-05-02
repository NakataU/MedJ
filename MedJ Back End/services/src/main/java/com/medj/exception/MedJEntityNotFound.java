package com.medj.exception;

import org.springframework.http.HttpStatus;

public class MedJEntityNotFound extends BaseMedJException {

    private static final String CODE = "ENTITY_NOT_FOUND";

    public MedJEntityNotFound() {
        super("Entity not found", CODE, HttpStatus.NOT_FOUND.value());
    }

    public MedJEntityNotFound(String message) {
        super(message, CODE, HttpStatus.NOT_FOUND.value());
    }

    public MedJEntityNotFound(String message, Throwable cause) {
        super(message, cause, CODE, HttpStatus.NOT_FOUND.value());
    }
}
