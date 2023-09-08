package dev.tarcisio.minebox.exception;

public class FileEmptyException extends RuntimeException {
  public FileEmptyException(String message) {
    super(message);
  }
}
