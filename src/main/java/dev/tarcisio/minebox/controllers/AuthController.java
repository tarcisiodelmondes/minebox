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
import dev.tarcisio.minebox.advice.ArgumentValidateMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Rotas de authenticação e criação de usuario")
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

  @Operation(summary = "Rota de login do usuario",
      description = "Recebe o email e password pelo body. A resposta é um objeto JwtResponse que contem o token, refreshtoken, id, name, email e tokenType",
      tags = {"authenticação", "post"})
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = {@Content(schema = @Schema(implementation = JwtResponse.class),
              mediaType = "application/json")}),
      @ApiResponse(responseCode = "400", description = "Credenciais incorretas",
          content = {@Content(mediaType = "text/plain")}),
      @ApiResponse(responseCode = "400",
          description = "Email ou Password mal formatado/nulo \t\n Credenciais incorretas (body=text/plain)",
          content = {@Content(schema = @Schema(implementation = ArgumentValidateMessage.class),
              mediaType = "application/json")})})
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      JwtResponse authenticate = authService.authenticate(loginRequest);
      return ResponseEntity.ok(authenticate);
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }
  }

  @Operation(summary = "Rota de criação de usuario",
      description = "Recebe name, email e password pelo body. A resposta é um texto puro",
      tags = {"criar", "post", "usuario"})
  @ApiResponses({@ApiResponse(responseCode = "201", content = {@Content(mediaType = "text/plain")}),
      @ApiResponse(responseCode = "400",
          description = "Email já esta em uso \t\n name, email ou password mal formatado/nulo (ArgumentValidateMessage)",
          content = {@Content(schema = @Schema(implementation = MessageResponse.class),
              mediaType = "application/json")})})
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
    try {
      userService.createUser(signupRequest);
      return ResponseEntity.status(201).body(new MessageResponse("User registered successfully"));
    } catch (EmailAreadyExistsException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new MessageResponse("Error: Email is already in use!"));
    }
  }

  @Operation(summary = "Rota que cria um novo token baseado no refresh token",
      description = "Recebe um refresh token pelo body. A resposta é um objeto TokenRefreshResponse que contém: token, refreshtoken e tokenType",
      tags = {"refresh token", "post"})
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = {@Content(schema = @Schema(implementation = TokenRefreshResponse.class),
              mediaType = "application/json")}),
      @ApiResponse(responseCode = "400",
          description = "Refresh token não existe no banco de dados \t\n refreshtoken mal formatado/nulo (ArgumentValidateMessage)",
          content = {@Content(schema = @Schema(implementation = MessageResponse.class),
              mediaType = "application/json")})})
  @PostMapping("/refreshtoken")
  public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
    String requestRefreshToken = request.getRefreshToken();

    try {
      TokenRefreshResponse tokenRefreshResponse =
          refreshTokenService.refreshToken(requestRefreshToken);
      return ResponseEntity.ok(tokenRefreshResponse);
    } catch (TokenRefreshException e) {
      return ResponseEntity.status(400)
          .body(new MessageResponse("Error: Token is not in the database!"));
    }
  }
}
