package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.AuthResponse;
import com.example.taskmanagement.dto.ForgotPasswordResponse;
import com.example.taskmanagement.dto.LoginRequest;
import com.example.taskmanagement.dto.RegisterRequest;
import com.example.taskmanagement.service.PasswordResetService;
import com.example.taskmanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private AuthResponse sampleAuthResponse() {
        return new AuthResponse("jwt-token-here", "haonguyen", "hao@example.com", "EMPLOYEE");
    }

    // ----- /api/auth/** is permitAll: must reach controller without token -----

    @Test
    void login_returns200_whenValidCredentials() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse());

        String body = """
                {"email": "hao@example.com", "password": "password123"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.data.role").value("EMPLOYEE"));
    }

    @Test
    void login_returns400_whenEmailMissing() throws Exception {
        String body = """
                {"password": "password123"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns201_whenValidPayload() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(sampleAuthResponse());

        String body = """
                {"username": "haonguyen", "email": "hao@example.com", "password": "password123"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("haonguyen"));
    }

    @Test
    void register_returns400_whenPasswordTooShort() throws Exception {
        String body = """
                {"username": "haonguyen", "email": "hao@example.com", "password": "abc"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ----- Quên mật khẩu -----

    @Test
    void forgotPassword_returns200_withGenericMessage() throws Exception {
        when(passwordResetService.requestReset(any()))
                .thenReturn(new ForgotPasswordResponse("Nếu email tồn tại...", null, null));

        String body = """
                {"email": "hao@example.com"}
                """;

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    void forgotPassword_returns400_whenEmailInvalid() throws Exception {
        String body = """
                {"email": "not-an-email"}
                """;

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_returns200_whenValidPayload() throws Exception {
        String body = """
                {"token": "some-token", "newPassword": "Password1!"}
                """;

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_returns400_whenPasswordWeak() throws Exception {
        // Thiếu số + ký tự đặc biệt → vi phạm @Pattern.
        String body = """
                {"token": "some-token", "newPassword": "onlyletters"}
                """;

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
