package dev.tarcisio.minebox.payload.request;

import jakarta.validation.constraints.NotBlank;

public class TokenRefreshRequest {
    @NotBlank(message = "Refresh token não pode ser nulo!")   
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
