package dev.tarcisio.minebox.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tarcisio.minebox.exception.EmailAlreadyExistsException;
import dev.tarcisio.minebox.exception.TokenRefreshException;
import dev.tarcisio.minebox.payload.request.LoginRequest;
import dev.tarcisio.minebox.payload.response.JwtResponse;
import dev.tarcisio.minebox.payload.response.TokenRefreshResponse;
import dev.tarcisio.minebox.services.AuthService;
import dev.tarcisio.minebox.services.RefreshTokenService;
import dev.tarcisio.minebox.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AuthService authService;

        @MockBean
        private UserService userService;

        @MockBean
        private RefreshTokenService refreshTokenService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        public void testRegisterUserShouldReturn201() throws Exception {
                String requestBody = "{ \"name\": \"Fulano 01\", \"email\": \"fulano@email.com\", \"password\": \"12345678\"}";

                mockMvc
                                .perform(post("/api/auth/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("User registered successfully"));
        }

        @Test
        public void testRegisterUserShouldReturn400BadRequest() throws Exception {
                // O "password" deve ser maior ou ingual a 8 caracteres, por isso esse teste
                // deve retornar 400
                String requestBody = "{ \"name\": \"Fulano 01\", \"email\": \"fulano@email.com\", \"password\": \"1234567\"}";

                mockMvc
                                .perform(post("/api/auth/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").isArray());
        }

        @Test
        public void testRegisterUserShouldReturn400EmailAlreadyExists()
                        throws Exception {
                String requestBody = "{ \"name\": \"Fulano 01\", \"email\": \"fulano@email.com\", \"password\": \"12345678\"}";

                Mockito
                                .doThrow(
                                                new EmailAlreadyExistsException())
                                .when(userService)
                                .createUser(Mockito.any());

                mockMvc
                                .perform(post("/api/auth/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(
                                                jsonPath("$.message").value("Error: Email is already in use!"));
        }

        @Test
        public void testAuthenticateUserShouldReturn200() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("fulano@email.com");
                loginRequest.setPassword("12345678");

                JwtResponse jwtResponse = new JwtResponse("token", "refreshtoken", "1",
                                "Fulano 01", "fulano@email.com");

                Mockito.when(authService.authenticate(Mockito.any()))
                                .thenReturn(jwtResponse);

                mockMvc
                                .perform(post("/api/auth/signin")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("token"))
                                .andExpect(jsonPath("$.refreshToken").value("refreshtoken"))
                                .andExpect(jsonPath("$.name").value("Fulano 01"));

                Mockito.verify(authService, Mockito.times(1)).authenticate(Mockito.any());
        }

        @Test
        public void testAuthenticateUserShouldReturn400BadCredentials()
                        throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("fulano@email.com");
                loginRequest.setPassword("123456789");

                Mockito.doThrow(new BadCredentialsException("Bad credentials"))
                                .when(authService)
                                .authenticate(Mockito.any());

                mockMvc
                                .perform(post("/api/auth/signin")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Bad credentials"));
        }

        @Test
        public void testRefreshTokenShouldReturn200() throws Exception {
                String refreshtoken = "{ \"refreshToken\": \"my-refresh-token\" }";

                TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse("token", "refreshtoken");

                Mockito.when(refreshTokenService.refreshToken(Mockito.any()))
                                .thenReturn(tokenRefreshResponse);

                mockMvc
                                .perform(post("/api/auth/refreshtoken")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(refreshtoken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").isString());
        }

        @Test
        public void testRefreshTokenShouldReturn400TokenRefreshException()
                        throws Exception {
                String refreshtoken = "{ \"refreshToken\": \"my-refresh-token\" }";

                Mockito.doThrow(new TokenRefreshException("token", "refreshtoken"))
                                .when(refreshTokenService)
                                .refreshToken(Mockito.any());

                mockMvc
                                .perform(post("/api/auth/refreshtoken")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(refreshtoken))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").isString());
        }
}
