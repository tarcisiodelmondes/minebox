package dev.tarcisio.minebox.advice;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class FileControllerAdvice {
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorMessage handleFileException(MaxUploadSizeExceededException ex, WebRequest request) {
    return new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(),
        "O arquivo não pode ter mais de 50MB de tamanho!",
        request.getDescription(false));
  }

}
