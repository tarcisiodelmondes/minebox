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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import dev.tarcisio.minebox.exception.EmailAlreadyExistsException;
import dev.tarcisio.minebox.exception.UserNotFoundException;
import dev.tarcisio.minebox.payload.request.UpdateUserRequest;
import dev.tarcisio.minebox.payload.response.UserResponse;
import dev.tarcisio.minebox.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

  @Operation(summary = "Rota de delete de usuario", description = "Recupera o id do usuario pelo contexto de autenticação. Deleta o usuario, seus arquivos e token de acesso")
  @ApiResponses({
      @ApiResponse(responseCode = "204", content = {
          @Content(schema = @Schema(implementation = Void.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "404", description = "Usuario não encontrado", content = {
          @Content(schema = @Schema(implementation = UserNotFoundException.class), mediaType = "application/json") })
  })
  @DeleteMapping(value = "/delete")
  public ResponseEntity<?> deleteById() throws UserNotFoundException {
    try {
      userService.delete();

      return ResponseEntity.status(204).build();
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    }
  }

  @Operation(summary = "Rota para atualizar o usuário", description = "Recebe os dados pelo body e recupera o usuário pelo token. Atualiza o nome e email, sendo nenhum deles obrigatorio")
  @ApiResponses({
      @ApiResponse(responseCode = "204", content = {
          @Content(schema = @Schema(implementation = Void.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "400", description = "Email já esta sendo usado \t\n Email mal formatado", content = {
          @Content(schema = @Schema(implementation = EmailAlreadyExistsException.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "404", description = "Usuario não encontrado", content = {
          @Content(schema = @Schema(implementation = UserNotFoundException.class), mediaType = "application/json") })
  })
  @PutMapping(value = "/update")
  public ResponseEntity<?> updateById(@Valid @RequestBody UpdateUserRequest updateUserRequest)
      throws UserNotFoundException {
    try {
      UserResponse userResponse = userService.update(updateUserRequest);

      return ResponseEntity.status(200).body(userResponse);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    }
  }

}
