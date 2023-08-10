package dev.tarcisio.minebox.controllers;

import dev.tarcisio.minebox.exception.EmailAreadyExistsException;
import dev.tarcisio.minebox.exception.TokenRefreshException;
import dev.tarcisio.minebox.payload.request.LoginRequest;
import dev.tarcisio.minebox.payload.request.SignupRequest;
import dev.tarcisio.minebox.payload.request.TokenRefreshRequest;
import dev.tarcisio.minebox.payload.response.JwtResponse;
import dev.tarcisio.minebox.payload.response.MessageResponse;
import dev.tarcisio.minebox.payload.response.TokenRefreshResponse;
import dev.tarcisio.minebox.services.AuthService;
import dev.tarcisio.minebox.services.RefreshTokenService;

import dev.tarcisio.minebox.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/signin")
    public ResponseEntity<?>
    authenticateUser(@Validated @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse authenticate = authService.authenticate(loginRequest);
            return ResponseEntity.ok(authenticate);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?>
    registerUser(@Validated @RequestBody SignupRequest signupRequest) {
        try {
            userService.createUser(signupRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully"));
        } catch (EmailAreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Email is already in use"));
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Validated @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            TokenRefreshResponse tokenRefreshResponse = refreshTokenService.refreshToken(requestRefreshToken);
            return ResponseEntity.ok(tokenRefreshResponse);
        } catch (TokenRefreshException e) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Token is not in the database!"));
        }
    }
}