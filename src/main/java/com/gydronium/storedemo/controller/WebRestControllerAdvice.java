package com.gydronium.storedemo.controller;

import com.gydronium.storedemo.dto.ValidationFailedMessageDto;
import com.gydronium.storedemo.exception.NotFoundException;
import com.gydronium.storedemo.exception.StoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class WebRestControllerAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationFailedMessageDto> handleValidationFailedException(Exception ex) {
        log.error(ex.getMessage(), ex);
        ValidationFailedMessageDto message = new ValidationFailedMessageDto();
        message.setCode(HttpStatus.BAD_REQUEST.value());
        message.setMessage("Validation Failed");
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = StoreException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationFailedMessageDto> handleValidationFailedException(StoreException ex) {
        log.error(ex.getMessage(), ex);
        ValidationFailedMessageDto message = new ValidationFailedMessageDto();
        message.setCode(HttpStatus.BAD_REQUEST.value());
        message.setMessage(ex.getMessage());
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationFailedMessageDto> handleNotFoundException(NotFoundException ex) {
        log.error(ex.getMessage(), ex);
        ValidationFailedMessageDto message = new ValidationFailedMessageDto();
        message.setCode(HttpStatus.NOT_FOUND.value());
        message.setMessage(ex.getMessage());
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }
}
