package dev.tarcisio.minebox.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FileRenameRequest {
  @NotBlank(message = "O campo nome não pode estar vazio!")
  @Size(min = 3, message = "Error: O nome tem que ter pelo menos 3 caracteres")
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
