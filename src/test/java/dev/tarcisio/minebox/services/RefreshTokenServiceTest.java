package dev.tarcisio.minebox.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.tarcisio.minebox.entities.RefreshToken;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.TokenRefreshException;
import dev.tarcisio.minebox.repositories.RefreshTokenRepository;
import dev.tarcisio.minebox.repositories.UserRepository;
import dev.tarcisio.minebox.security.jwt.JwtUtils;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(RefreshTokenServiceTest.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class RefreshTokenServiceTest {
  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private UserRepository userRepository;

  @Mock private JwtUtils jwtUtils;

  @InjectMocks private RefreshTokenService refreshTokenService;

  @Value("${jwt.refreshTokenExpiration}") private Long refreshTokenExpiration;

  @BeforeEach
  public void setUp() {
    Field field;
    try {
      field = refreshTokenService.getClass().getDeclaredField(
          "refreshTokenExpiration");
      field.setAccessible(true);
      field.set(refreshTokenService, 6000L);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testFindByTokenShouldReturnRefreshToken() {
    Mockito.when(refreshTokenRepository.findByToken("TOKEN"))
        .thenReturn(Optional.of(new RefreshToken()));

    RefreshToken refreshToken = refreshTokenService.findByToken("TOKEN").get();

    assertNotNull(refreshToken);
  }

  @Test
  public void testCreateRefreshTokenShouldReturnRefreshToken() {
    User user = new User("Fulano", "test@email.com", "12345678");

    Mockito.when(userRepository.findById(user.getId()))
        .thenReturn(Optional.of(user));
    Mockito.when(refreshTokenRepository.findByUserId(user.getId()))
        .thenReturn(Optional.ofNullable(null));

    RefreshToken refreshToken =
        refreshTokenService.createRefreshToken(user.getId());

    assertNotNull(refreshToken);

    Mockito.verify(refreshTokenRepository, Mockito.times(0))
        .deleteByUser(Mockito.any());
  }

  @Test
  public void
  testCreateRefreshTokenShouldDeleteRefreshTokenAndCreateNewRefreshToken() {
    User user = new User("Fulano", "test@email.com", "12345678");

    Mockito.when(userRepository.findById(user.getId()))
        .thenReturn(Optional.of(user));
    Mockito.when(refreshTokenRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(new RefreshToken()));

    RefreshToken refreshToken =
        refreshTokenService.createRefreshToken(user.getId());

    assertNotNull(refreshToken);

    Mockito.verify(refreshTokenRepository, Mockito.times(1))
        .deleteByUser(Mockito.any());
  }

  @Test
  public void testVerifyExpirationShouldReturnRefreshToken() {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken("TOKEN");
    refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

    RefreshToken result = refreshTokenService.verifyExpiration(refreshToken);

    assertNotNull(result);
    assertEquals("TOKEN", result.getToken());
  }

  @Test
  public void testVerifyExpirationShouldThrowTokenRefreshException() {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setExpiryDate(Instant.now().minusSeconds(60));

    assertThrows(TokenRefreshException.class,
                 () -> refreshTokenService.verifyExpiration(refreshToken));
  }

  @Test
  public void testRefreshTokenShouldReturnTokenRefreshResponse() {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken("token");
    refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

    User user = new User("Fulano", "test@email.com", "12345678");
    refreshToken.setUser(user);

    Mockito.when(refreshTokenService.findByToken(refreshToken.getToken()))
        .thenReturn(Optional.of(refreshToken));
    Mockito
        .when(jwtUtils.generateTokenFromUsername(
            refreshToken.getUser().getId()))
        .thenReturn("TOKEN");

    var result = refreshTokenService.refreshToken(refreshToken.getToken());
    assertEquals("TOKEN", result.getToken());
  }

  @Test
  public void testRefreshTokenShouldThrowTokenRefreshException() {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken("token");
    refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

    User user = new User("Fulano", "test@email.com", "12345678");
    refreshToken.setUser(user);

    Mockito.when(refreshTokenService.findByToken(refreshToken.getToken()))
        .thenReturn(Optional.ofNullable(null));

    assertThrows(
        TokenRefreshException.class,
        () -> refreshTokenService.refreshToken(refreshToken.getToken()));
  }

  @Test
  public void testDeleteByUserIdShouldReturnInt() {
    User user = new User();
    user.setId("user_id");

    Mockito.when(userRepository.findById(user.getId()))
        .thenReturn(Optional.of(user));
    Mockito.when(refreshTokenRepository.deleteByUser(user)).thenReturn(1);

    int result = refreshTokenService.deleteByUserId(user.getId());

    assertEquals(1, result);
  }
}
