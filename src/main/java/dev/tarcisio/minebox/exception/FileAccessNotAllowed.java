package dev.tarcisio.minebox.exception;

public class FileAccessNotAllowed extends RuntimeException {
  public FileAccessNotAllowed(String message) {
    super(message);
  }

}
