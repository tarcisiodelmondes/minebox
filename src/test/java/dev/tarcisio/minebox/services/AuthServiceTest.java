package dev.tarcisio.minebox.services;

import dev.tarcisio.minebox.entities.RefreshToken;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.payload.request.LoginRequest;
import dev.tarcisio.minebox.payload.response.JwtResponse;
import dev.tarcisio.minebox.security.jwt.JwtUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtUtils jwtUtils;

  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private AuthService authService;

  @Test
  public void whenUserAuthenticateShouldReturnJwtResponse() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@email.com");
    loginRequest.setPassword("12345678");

    UserDetailsImpl userDetails =
        new UserDetailsImpl("UUID", "Fulano", "test@email.com", "12345678");

    Mockito.when(authenticationManager.authenticate(
             Mockito.any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(new UsernamePasswordAuthenticationToken(
            userDetails, "hashedPassword", userDetails.getAuthorities()));

    Mockito.when(jwtUtils.generateJwtToken(userDetails)).thenReturn("jwtToken");

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId("UUID");
    User user = new User("Fulano", "test@email.com", "12345678");
    refreshToken.setUser(user);
    refreshToken.setExpiryDate(Instant.now());
    refreshToken.setToken("refreshToken");

    Mockito.when(refreshTokenService.createRefreshToken("UUID"))
        .thenReturn(refreshToken);

    JwtResponse jwtResponse = authService.authenticate(loginRequest);

    assertEquals("jwtToken", jwtResponse.getToken());
    assertEquals("refreshToken", jwtResponse.getRefreshToken());
    assertEquals("UUID", jwtResponse.getId());
    assertEquals("Fulano", jwtResponse.getName());
    assertEquals("test@email.com", jwtResponse.getEmail());

    Mockito.verify(authenticationManager, Mockito.times(1))
        .authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    Mockito.verify(jwtUtils, Mockito.times(1)).generateJwtToken(userDetails);
    Mockito.verify(refreshTokenService, Mockito.times(1)).createRefreshToken("UUID");
  }
}
