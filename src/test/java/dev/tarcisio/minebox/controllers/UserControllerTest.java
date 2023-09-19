package dev.tarcisio.minebox.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.EmailAlreadyExistsException;
import dev.tarcisio.minebox.exception.UserNotFoundException;
import dev.tarcisio.minebox.payload.request.UpdateUserRequest;
import dev.tarcisio.minebox.payload.response.UserResponse;
import dev.tarcisio.minebox.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Captor
  private ArgumentCaptor<UpdateUserRequest> updateUserRequestCaptor;

  @MockBean
  UserService userService;

  @Test
  public void testFindByIdShouldReturn200WithUserResponse() throws Exception {
    User user = new User();
    user.setId("user_id");
    user.setName("Fulano");
    user.setEmail("test@email.com");
    user.setPassword("12345678");

    UserResponse userResponse = new UserResponse(user);

    Mockito.when(userService.findUserById("user_id")).thenReturn(userResponse);

    mockMvc
        .perform(get("/api/user/user_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("user_id")).andExpect(jsonPath("$.name").value("Fulano"));
  }

  @Test
  public void testFindByIdShouldReturn401WithoutBody() throws Exception {
    Mockito.when(userService.findUserById(Mockito.anyString())).thenThrow(new UserNotFoundException("User not found"));

    mockMvc
        .perform(get("/api/user/user_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound()).andExpect(content().string(""));
  }

  @Test
  public void testDeleteByIdShouldReturn204WithoutBody() throws Exception {
    Mockito.doNothing().when(userService).delete("user_id");

    mockMvc
        .perform(delete("/api/user/user_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent()).andExpect(content().string(""));
  }

  @Test
  public void testDeleteByIdShouldReturn404() throws Exception {
    Mockito.doThrow(new UserNotFoundException("Error: o usuario não existe!")).when(userService).delete("user_id");

    mockMvc
        .perform(delete("/api/user/user_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound()).andExpect(content().string("Error: o usuario não existe!"));
  }

  @Test
  public void testUpdateByIdShouldReturn200WithUserResponse() throws Exception {
    UpdateUserRequest userRequest = new UpdateUserRequest();
    userRequest.setName("Fulano");
    userRequest.setEmail("novo@email.com");

    User user = new User("Fulano", userRequest.getEmail(), "12345678");
    UserResponse userResponse = new UserResponse(user);

    Mockito.when(userService.update(Mockito.any(UpdateUserRequest.class))).thenReturn(userResponse);

    String requestBody = objectMapper.writeValueAsString(userRequest);

    mockMvc
        .perform(put("/api/user/update").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
            .contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(user.getId()))
        .andExpect(jsonPath("$.name").value("Fulano"))
        .andExpect(jsonPath("$.email").value("novo@email.com"));

    Mockito.verify(userService).update(updateUserRequestCaptor.capture());

    // Verifica se o valores do body veio corretamente
    UpdateUserRequest capturedUpdateUserRequest = updateUserRequestCaptor.getValue();
    assertEquals(userRequest.getName(), capturedUpdateUserRequest.getName());
    assertEquals(userRequest.getEmail(), capturedUpdateUserRequest.getEmail());
  }

  @Test
  public void testUpdateByIdShouldReturn400WithEmailAlreadyExistsException() throws Exception {
    UpdateUserRequest userRequest = new UpdateUserRequest();
    userRequest.setName("Fulano");
    userRequest.setEmail("email_ja_existe@email.com");

    Mockito.when(userService.update(Mockito.any(UpdateUserRequest.class))).thenThrow(new EmailAlreadyExistsException());

    String requestBody = objectMapper.writeValueAsString(userRequest);

    mockMvc
        .perform(put("/api/user/update").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
            .contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Error: este email já esta sendo usado!"));
  }

}
