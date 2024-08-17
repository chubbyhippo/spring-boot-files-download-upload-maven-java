package io.github.chubbyhippo.updown.presentation;

import io.github.chubbyhippo.updown.domain.EmptyFileException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleEmptyFileException(EmptyFileException emptyFileException) {
        return emptyFileException.getMessage();
    }

}
