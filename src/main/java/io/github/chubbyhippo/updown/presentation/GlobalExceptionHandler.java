package io.github.chubbyhippo.updown.presentation;

import io.github.chubbyhippo.updown.domain.EmptyFileException;
import io.github.chubbyhippo.updown.domain.StorageException;
import io.github.chubbyhippo.updown.domain.StorageFileNotFoundException;
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

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleStorageException(StorageException storageException) {
        return storageException.getMessage();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleStorageFileNotFoundException(StorageFileNotFoundException storageFileNotFoundException) {
        return storageFileNotFoundException.getMessage();
    }

}
