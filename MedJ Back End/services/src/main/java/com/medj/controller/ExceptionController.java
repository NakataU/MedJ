package com.medj.controller;

import com.medj.exception.BaseMedJException;
import com.medj.view.ErrorView;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@RestControllerAdvice
public class ExceptionController {

    @Autowired
    private MessageSource messages;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorView> handleConflict(
            IllegalArgumentException e,
            HttpServletRequest request) {

        ErrorView ev = new ErrorView();
        ev.setExMessage(e.getMessage());
        ev.setTime(java.time.LocalDateTime.now());
        ev.setLogId(java.util.UUID.randomUUID().toString());
        ev.setPath(request.getRequestURI());
        ev.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ev);
    }

    @ExceptionHandler({BadCredentialsException.class, JwtException.class})
    public ResponseEntity<ErrorView> handleAuthException(
            RuntimeException e,
            HttpServletRequest request) {

        ErrorView ev = new ErrorView();
        ev.setExMessage(e.getMessage());
        ev.setTime(java.time.LocalDateTime.now());
        ev.setLogId(java.util.UUID.randomUUID().toString());
        ev.setPath(request.getRequestURI());
        ev.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ev);
    }

    @ExceptionHandler(BaseMedJException.class)
    public ResponseEntity<ErrorView> handleMedJException(
            BaseMedJException e,
            HttpServletRequest request,
            Locale locale) {

        ErrorView ev = new ErrorView();

        String localizedMessage = messages.getMessage(
                e.getCode(),
                null,
                e.getMessage(),
                locale
        );

        ev.setExMessage(localizedMessage);
        ev.setTime(LocalDateTime.now());
        ev.setLogId(UUID.randomUUID().toString());
        ev.setPath(request.getRequestURI());
        ev.setMethod(request.getMethod());

        return ResponseEntity
                .status(e.getStatus())
                .body(ev);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorView> handleGenericException(
            Exception e,
            HttpServletRequest request) {

        ErrorView ev = new ErrorView();
        ev.setExMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
        ev.setTime(LocalDateTime.now());
        ev.setLogId(UUID.randomUUID().toString());
        ev.setPath(request.getRequestURI());
        ev.setMethod(request.getMethod());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ev);
    }
}