package com.parasol.BaaS.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler({
            MethodArgumentNotValidException.class, // 유효하지 않은 인수 값
    })
    public ResponseEntity<Object> InvalidRequestException(final MethodArgumentNotValidException ex) {
        log.error("Invalid Request", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler({
            IllegalArgumentException.class // 유효하지 않은 인수 값 또는 필요한 인수가 들어오지 않았을 때
    })
    public ResponseEntity<Object> InsufficientRequestException(final IllegalArgumentException ex) {
        log.error("Insufficient Request", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler({
            AccessDeniedException.class // 로그인이 필요한 페이지에 로그인 없이 접근
    })
    public ResponseEntity<Object> AccessDeniedException(final AccessDeniedException ex) {
        log.error("Access Denied", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler({
            IllegalAccessException.class // 일정 권한이 필요한 페이지에 권한 없이 접근
    })
    public ResponseEntity<Object> ForbiddenException(final IllegalAccessException ex) {
        log.error("Forbidden", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler({
            NoSuchElementException.class
    })
    public ResponseEntity<Object> NotFoundException(final NoSuchElementException ex) {
        log.error("Not Found", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class
    })
    public ResponseEntity<Object> MethodNotSupportedException(final HttpRequestMethodNotSupportedException ex) {
        log.error("Method Not Supported", ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @ExceptionHandler({
            ResponseStatusException.class // 기타 모든 오류
    })
    public ResponseEntity<Object> ResponseStatusException(final ResponseStatusException ex) {
        log.error("Status handled", ex);
        return ResponseEntity.status(ex.getStatus()).build();
    }
}
