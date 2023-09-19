package dev.tarcisio.minebox.advice;

import dev.tarcisio.minebox.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
public class EmailAlreadyExistsAdvice {
  @ExceptionHandler(EmailAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorMessage handleEmailAlreadyExistsException(EmailAlreadyExistsException ex,
      WebRequest request) {
    return new ErrorMessage(
        HttpStatus.BAD_REQUEST.value(),
        new Date(),
        "Error: este email já esta sendo usado!",
        request.getDescription(false));
  }
}
