package dev.tarcisio.minebox.services;

import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.EmailAreadyExistsException;
import dev.tarcisio.minebox.payload.request.SignupRequest;
import dev.tarcisio.minebox.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Transactional
    public void createUser(SignupRequest signupRequest) {
        var isUserAlreadyExists = userRepository.findByEmail(signupRequest.getEmail());

        if (isUserAlreadyExists.isPresent()) {
            throw new EmailAreadyExistsException("Error: Email is already in use!");
        }

        var newUser = new User(signupRequest.getName(), signupRequest.getEmail(), encoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
    }
}