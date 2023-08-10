package dev.tarcisio.minebox.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {
    @NotBlank(message = "O campo 'Nome' não pode estar vazio")
    private String name;

    @NotBlank(message = "O campo 'Email' não pode estar vazio")
    @Size(max = 255, message = "Email so pode ter no maximo 255 caracteres")
    @Email(message = "Email invalido!")
    private String email;

    @NotBlank(message = "Email não pode estar vazio!")
    @Size(min = 8, message = "A senha tem que ter no minimo 8 caracteres")
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
