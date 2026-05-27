package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.CreateEmployeeRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private EmployeeService employeeService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private Employee sampleEmployee() {
        Employee e = new Employee();
        e.setEmployeeId(1L);
        e.setFirstName("Hảo");
        e.setLastName("Nguyễn");
        e.setPosition("Developer");
        e.setDepartment("Engineering");
        return e;
    }

    // ----- Authorization gates -----

    // Project security config không có custom AuthenticationEntryPoint, nên Spring trả
    // 403 (không phải 401) cho request thiếu bearer token. Document hành vi thực tế.
    @Test
    void getAllEmployees_returns403_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllEmployees_returns403_whenEmployeeRole() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createEmployee_returns403_whenEmployeeRole() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest(
                "Hảo", "Nguyễn", "Developer", "Eng", "G1", null, null);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ----- Happy paths -----

    @Test
    @WithMockUser(roles = "MANAGER")
    void getAllEmployees_returns200_whenManager() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sampleEmployee()));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].firstName").value("Hảo"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getMyProfile_returns200_whenEmployee() throws Exception {
        when(employeeService.getMyProfile(any(Authentication.class))).thenReturn(sampleEmployee());

        mockMvc.perform(get("/api/employees/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_returns201_whenAdmin() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(sampleEmployee());

        CreateEmployeeRequest req = new CreateEmployeeRequest(
                "Hảo", "Nguyễn", "Developer", "Eng", "G1", null, null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstName").value("Hảo"));
    }
}
