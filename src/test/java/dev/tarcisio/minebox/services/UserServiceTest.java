package dev.tarcisio.minebox.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.tarcisio.minebox.entities.File;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.EmailAlreadyExistsException;
import dev.tarcisio.minebox.exception.UserNotFoundException;
import dev.tarcisio.minebox.payload.request.SignupRequest;
import dev.tarcisio.minebox.payload.request.UpdateUserRequest;
import dev.tarcisio.minebox.payload.response.UserResponse;
import dev.tarcisio.minebox.repositories.FileRepository;
import dev.tarcisio.minebox.repositories.UserRepository;
import dev.tarcisio.minebox.utils.S3Utils;
import jakarta.persistence.EntityManager;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private UserService userService;

  @BeforeEach
  public void setUp() {
    // Configure o contexto de segurança para simular um usuário autenticado
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        new UserDetailsImpl("id", "Fulano", "test@email.com", "12345678"), null);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

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

    Assertions.assertThrows(EmailAlreadyExistsException.class,
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

  @Test
  public void whenUpdateShouldReturnUserResponse() {
    // Testa o update com um nome e email novo
    User user = new User("Fulano", "fulano@email.com", "12345678");

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));

    UpdateUserRequest updateUserRequest = new UpdateUserRequest();
    updateUserRequest.setName("Novo Fulano");
    updateUserRequest.setEmail("novo@email.com");

    Mockito.when(userRepository.existsByEmail(Mockito.any())).thenReturn(false);
    Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

    UserResponse result = userService.update(updateUserRequest);

    assertEquals("Novo Fulano", result.getName());
    assertEquals("novo@email.com", result.getEmail());

    // Testa o update apenas com um nome novo
    UpdateUserRequest updateUserRequest2 = new UpdateUserRequest();
    updateUserRequest2.setName("Novo Fulano 2");

    UserResponse result2 = userService.update(updateUserRequest2);
    assertEquals("Novo Fulano 2", result2.getName());
    assertEquals("novo@email.com", result2.getEmail());

    // Testa o update apenas com um email novo
    UpdateUserRequest updateUserRequest3 = new UpdateUserRequest();
    updateUserRequest3.setEmail("novo3@email.com");

    UserResponse result3 = userService.update(updateUserRequest3);
    assertEquals("Novo Fulano 2", result3.getName());
    assertEquals("novo3@email.com", result3.getEmail());

    // Testa o update com nome e email nulo
    UpdateUserRequest updateUserRequest4 = new UpdateUserRequest();

    UserResponse result4 = userService.update(updateUserRequest4);
    assertEquals("Novo Fulano 2", result4.getName());
    assertEquals("novo3@email.com", result3.getEmail());

  }

  @Test
  public void whenUpdateShouldThrowUserNotFoundException() {
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(null));

    UpdateUserRequest updateUserRequest = new UpdateUserRequest();
    updateUserRequest.setName("Novo Fulano");
    updateUserRequest.setEmail("novo@email.com");

    assertThrows(UserNotFoundException.class, () -> userService.update(updateUserRequest));
  }

  @Test
  public void whenUpdateShouldThrowEmailAlreadyExistsException() {
    User user = new User("Fulano", "fulano@email.com", "12345678");

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
    Mockito.when(userRepository.existsByEmail(Mockito.any())).thenReturn(true);

    UpdateUserRequest updateUserRequest = new UpdateUserRequest();
    updateUserRequest.setName("Novo Fulano");
    updateUserRequest.setEmail("novo@email.com");

    assertThrows(EmailAlreadyExistsException.class,
        () -> userService.update(updateUserRequest));

  }
}
