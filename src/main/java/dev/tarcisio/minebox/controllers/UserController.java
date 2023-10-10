package dev.tarcisio.minebox.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.tarcisio.minebox.exception.EmailAlreadyExistsException;
import dev.tarcisio.minebox.exception.UnauthorizedAccessException;
import dev.tarcisio.minebox.exception.UserNotFoundException;
import dev.tarcisio.minebox.payload.request.UpdateUserRequest;
import dev.tarcisio.minebox.payload.response.UserResponse;
import dev.tarcisio.minebox.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Tag(name = "Rota do usuario", description = "Aqui vai estar as rotas referente ao usuario")
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
  @Autowired
  private UserService userService;

  @Operation(summary = "Rota de busca de usuario por id", description = "Recebe o id pelo path param. A resposta é um objeto UserResponse que contem o id, name e email")
  @ApiResponses({
      @ApiResponse(responseCode = "200", content = {
          @Content(schema = @Schema(implementation = UserResponse.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "404", description = "Usuario não encontrado", content = {
          @Content(schema = @Schema(implementation = UserNotFoundException.class), mediaType = "application/json") })
  })
  @GetMapping(value = "/{id}")
  public ResponseEntity<?> findById(@PathVariable String id) throws UserNotFoundException {
    try {
      UserResponse userResponse = userService.findUserById(id);

      return ResponseEntity.status(200).body(userResponse);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(404).build();
    }
  }

  @Operation(summary = "Rota de delete de usuario", description = "Recebe o id pelo path da url. Deleta o usuario, seus arquivos e token de acesso")
  @ApiResponses({
      @ApiResponse(responseCode = "204", content = {
          @Content(schema = @Schema(implementation = Void.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "404", description = "Usuario não encontrado", content = {
          @Content(mediaType = "plain/text") }),
      @ApiResponse(responseCode = "403", description = "O usuário não tem permisão para acessar este recurso", content = {
          @Content(mediaType = "plain/text") })
  })
  @DeleteMapping(value = "/delete/{id}")
  public ResponseEntity<?> deleteById(@PathVariable @NotBlank String id) throws UserNotFoundException {
    try {
      userService.delete(id);

      return ResponseEntity.status(204).build();
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (UnauthorizedAccessException e) {
      return ResponseEntity.status(403).body(e.getMessage());
    }
  }

  @Operation(summary = "Rota para atualizar o usuário", description = "Recebe os dados pelo body e pega o id do usuário pelo path da url. Atualiza o nome e email, sendo nenhum deles obrigatorio")
  @ApiResponses({
      @ApiResponse(responseCode = "204", content = {
          @Content(schema = @Schema(implementation = Void.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "400", description = "Email já esta sendo usado \t\n Email mal formatado", content = {
          @Content(schema = @Schema(implementation = EmailAlreadyExistsException.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "404", description = "Usuario não encontrado", content = {
          @Content(schema = @Schema(implementation = UserNotFoundException.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "403", description = "", content = {
          @Content(mediaType = "plain/text") })
  })
  @PutMapping(value = "/update/{id}")
  public ResponseEntity<?> updateById(@PathVariable @NotBlank String id,
      @Valid @RequestBody UpdateUserRequest updateUserRequest)
      throws UserNotFoundException {
    try {
      UserResponse userResponse = userService.update(id, updateUserRequest);

      return ResponseEntity.status(200).body(userResponse);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (UnauthorizedAccessException e) {
      return ResponseEntity.status(403).body(e.getMessage());
    }
  }

}
