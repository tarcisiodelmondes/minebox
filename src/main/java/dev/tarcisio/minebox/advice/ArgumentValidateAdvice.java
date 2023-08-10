package dev.tarcisio.minebox.advice;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ArgumentValidateAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ArgumentValidateMessage handleArgumentValidateException(MethodArgumentNotValidException ex, WebRequest request) {
      List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();

        return new ArgumentValidateMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                errors,
                request.getDescription(false)
        );
    }
}
