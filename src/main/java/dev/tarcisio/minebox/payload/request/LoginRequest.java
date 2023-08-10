package dev.tarcisio.minebox.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "Email não pode estar vazio!")
    @Size(max = 255, message = "Email so pode ter no maximo 255 caracteres")
    @Email(message = "Email invalido!")
    private String email;

    @NotBlank(message = "Senha não pode estar vazia!")
    @Size(min = 8, message = "A senha tem que ter no minimo 8 caracteres")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
