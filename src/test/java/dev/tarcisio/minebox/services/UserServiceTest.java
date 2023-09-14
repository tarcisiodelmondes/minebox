package dev.tarcisio.minebox.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.tarcisio.minebox.entities.File;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.EmailAreadyExistsException;
import dev.tarcisio.minebox.exception.UserNotFoundException;
import dev.tarcisio.minebox.payload.request.SignupRequest;
import dev.tarcisio.minebox.payload.response.UserResponse;
import dev.tarcisio.minebox.repositories.FileRepository;
import dev.tarcisio.minebox.repositories.UserRepository;
import dev.tarcisio.minebox.utils.S3Utils;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private FileRepository fileRepository;

  @Mock
  private S3Utils s3Utils;

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private UserService userService;

  @Test
  public void whenSaveUserShouldReturnVoid() {
    SignupRequest newUser = new SignupRequest();
    newUser.setEmail("test@email.com");
    newUser.setName("Fulano");
    newUser.setPassword("12345678");

    Mockito.when(userRepository.save(Mockito.any(User.class)))
        .thenReturn(new User());

    Mockito.verifyNoInteractions(userRepository);

    userService.createUser(newUser);

    Mockito.verify(userRepository, Mockito.times(1))
        .save(Mockito.any(User.class));
  }

  @Test
  public void whenSaveUserShouldReturnEmailAlreadyExistsException() {
    SignupRequest newUser = new SignupRequest();
    newUser.setEmail("test@email.com");
    newUser.setName("Fulano 01");
    newUser.setPassword("12345678");

    User existingUser = new User("Fulano 02", "test@email.com", "87654321");

    Mockito.when(userRepository.findByEmail("test@email.com"))
        .thenReturn(Optional.of(existingUser));

    Assertions.assertThrows(EmailAreadyExistsException.class,
        () -> userService.createUser(newUser));
  }

  @Test
  public void whenFindUserByIdShouldReturnUserResponse() {
    User user = new User();
    user.setId("user_id");
    user.setEmail("test@email.com");
    user.setName("Fulano");
    user.setPassword("12345678");

    Mockito.when(userRepository.findById(user.getId()))
        .thenReturn(Optional.of(user));

    UserResponse result = userService.findUserById("user_id");

    assertEquals("user_id", result.getId());
  }

  @Test
  public void whenFindUserByIdShouldReturnUserNotFoundException() {
    Mockito.when(userRepository.findById("um_id_qualquer"))
        .thenReturn(Optional.ofNullable(null));

    assertThrows(UserNotFoundException.class,
        () -> userService.findUserById("um_id_qualquer"));
  }

  @Test
  public void whenDeleteShouldReturnVoid() {
    String userId = "user_id";

    Mockito.when(userRepository.existsById(userId)).thenReturn(true);
    Mockito.when(fileRepository.findAllByUserId(userId)).thenReturn(Arrays.asList(new File(), new File()));
    Mockito.doNothing().when(s3Utils).deleteFile(Mockito.any());

    userService.delete(userId);

    Mockito.verify(fileRepository, Mockito.times(2)).deleteById(Mockito.any());
    Mockito.verify(refreshTokenService).deleteByUserId(userId);
    Mockito.verify(userRepository).deleteById(userId);
  }

  @Test()
  public void whenDeleteShouldThrownUserNotFoundException() {
    String userId = "id_que_nao_existe";

    Mockito.when(userRepository.existsById(userId)).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> userService.delete(userId));
  }
}
