package dev.tarcisio.minebox.services;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.tarcisio.minebox.entities.File;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.EmailAreadyExistsException;
import dev.tarcisio.minebox.exception.UserNotFoundException;
import dev.tarcisio.minebox.payload.request.SignupRequest;
import dev.tarcisio.minebox.payload.response.UserResponse;
import dev.tarcisio.minebox.repositories.FileRepository;
import dev.tarcisio.minebox.repositories.UserRepository;
import dev.tarcisio.minebox.utils.S3Utils;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FileRepository fileRepository;
  @Autowired
  private S3Utils s3Utils;
  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  PasswordEncoder encoder;

  @Transactional
  public void createUser(SignupRequest signupRequest) {
    var isUserAlreadyExists = userRepository.findByEmail(signupRequest.getEmail());

    if (isUserAlreadyExists.isPresent()) {
      throw new EmailAreadyExistsException("Error: Email is already in use!");
    }

    var newUser = new User(signupRequest.getName(), signupRequest.getEmail(),
        encoder.encode(signupRequest.getPassword()));
    userRepository.save(newUser);
  }

  public UserResponse findUserById(String id) {
    Optional<User> isUserExists = userRepository.findById(id);

    if (!isUserExists.isPresent()) {
      throw new UserNotFoundException("User not found!");
    }

    return new UserResponse(isUserExists.get());
  }

  @Transactional
  public void delete(String id) {
    boolean isUserExists = userRepository.existsById(id);

    if (!isUserExists) {
      throw new UserNotFoundException("Error: o usuario não existe!");
    }

    List<File> files = fileRepository.findAllByUserId(id);

    for (File file : files) {
      s3Utils.deleteFile(file.getS3FileKey());
      fileRepository.deleteById(file.getId());
    }

    refreshTokenService.deleteByUserId(id);
    userRepository.deleteById(id);

  }
}
