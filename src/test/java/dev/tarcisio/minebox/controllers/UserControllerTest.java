package dev.tarcisio.minebox.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.UserNotFoundException;
import dev.tarcisio.minebox.payload.response.UserResponse;
import dev.tarcisio.minebox.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

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
}
