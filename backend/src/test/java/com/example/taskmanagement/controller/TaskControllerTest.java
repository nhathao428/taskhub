package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TaskService taskService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private Task sampleTask() {
        Task t = new Task();
        t.setTaskId(1L);
        t.setTitle("Triển khai API");
        t.setStatus("pending");
        t.setDueDate(LocalDate.now().plusDays(7));
        return t;
    }

    // ----- Authorization gate (URL-pattern rules from SecurityFilterChain) -----

    // JwtAuthenticationFilter trả 403 (không có entry point custom) khi request thiếu
    // bearer token trên endpoint protected — đây là behavior thực tế của project.
    @Test
    void getAllTasks_returns403_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createTask_returns403_whenEmployeeRole() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest(
                "x", "y", null, LocalDate.now(), "pending", null, null);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ----- Happy path -----

    @Test
    @WithMockUser(roles = "MANAGER")
    void getAllTasks_returns200_whenManager() throws Exception {
        when(taskService.getAllTasks(any(Authentication.class)))
                .thenReturn(List.of(sampleTask()));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data[0].title").value("Triển khai API"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getMyTasks_returns200_whenEmployee() throws Exception {
        when(taskService.getMyTasks(any(Authentication.class)))
                .thenReturn(List.of(sampleTask()));

        mockMvc.perform(get("/api/tasks/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data[0].taskId").value(1));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createTask_returns201_whenManager() throws Exception {
        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(sampleTask());

        CreateTaskRequest req = new CreateTaskRequest(
                "Triển khai API", "desc", null, LocalDate.now().plusDays(7), "pending", null, null);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Triển khai API"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getTaskById_propagatesAccessDenied() throws Exception {
        when(taskService.getTaskById(eq(99L), any(Authentication.class)))
                .thenThrow(new AccessDeniedException("forbidden"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isForbidden());
    }
}
