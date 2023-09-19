package dev.tarcisio.minebox.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
  private String name;

  @Size(max = 255, message = "Email so pode ter no maximo 255 caracteres")
  @Email(message = "Email invalido!")
  private String email;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
