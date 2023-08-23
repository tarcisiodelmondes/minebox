package dev.tarcisio.minebox.services;

import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.EmailAreadyExistsException;
import dev.tarcisio.minebox.payload.request.SignupRequest;
import dev.tarcisio.minebox.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void whenSaveUserShouldReturnVoid() {
        SignupRequest newUser = new SignupRequest();
        newUser.setEmail("test@email.com");
        newUser.setName("Fulano");
        newUser.setPassword("12345678");

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(new User());

        Mockito.verifyNoInteractions(userRepository);

        userService.createUser(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void whenSaveUserShouldReturnEmailAlreadyExistsException() {
        SignupRequest newUser = new SignupRequest();
        newUser.setEmail("test@email.com");
        newUser.setName("Fulano 01");
        newUser.setPassword("12345678");

        User existingUser = new User("Fulano 02", "test@email.com", "87654321");

        Mockito.when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(existingUser));

        Assertions.assertThrows(EmailAreadyExistsException.class, () -> userService.createUser(newUser));
    }
}
