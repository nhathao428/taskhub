package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.EmployeeSuggestionDTO;
import com.example.taskmanagement.dto.SuggestionRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.Skill;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.repository.AttendanceRepository;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.SkillRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(AiSuggestionService.class);
    private static final int EXPECTED_WORKING_DAYS = 22;
    private static final int TOP_N = 5;
    /** Penalty applied to workload score per active task (each task reduces score by 20%). */
    private static final double WORKLOAD_PENALTY_FACTOR = 0.2;
    /** Neutral skill match score used when no required skills are specified. */
    private static final double DEFAULT_SKILL_MATCH_SCORE = 0.5;
    /** Neutral performance score used when no historical performance data is available. */
    private static final double DEFAULT_PERFORMANCE_SCORE = 0.5;

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;
    private final SkillRepository skillRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String openAiModel;

    public AiSuggestionService(EmployeeRepository employeeRepository,
                                TaskRepository taskRepository,
                                AttendanceRepository attendanceRepository,
                                SkillRepository skillRepository,
                                RestClient.Builder restClientBuilder,
                                ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
        this.attendanceRepository = attendanceRepository;
        this.skillRepository = skillRepository;
        this.restClient = restClientBuilder.baseUrl("https://api.openai.com").build();
        this.objectMapper = objectMapper;
    }

    @Cacheable(value = "ai_suggestions", key = "#request.cacheKey")
    public List<EmployeeSuggestionDTO> recommendEmployees(SuggestionRequest request) {
        if (request == null || request.getTaskTitle() == null || request.getTaskTitle().isBlank()) {
            throw new IllegalArgumentException("Task title is required");
        }

        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            return List.of();
        }

        List<Long> employeeIds = employees.stream().map(Employee::getEmployeeId).collect(Collectors.toList());

        Map<Long, List<Skill>> skillsByEmployee = skillRepository.findByEmployeeEmployeeIdIn(employeeIds)
                .stream().collect(Collectors.groupingBy(s -> s.getEmployee().getEmployeeId()));

        Map<Long, Long> activeTasksByEmployee = taskRepository
                .findByAssignedToEmployeeIdInAndStatusIn(employeeIds, List.of("pending", "in_progress"))
                .stream().collect(Collectors.groupingBy(t -> t.getAssignedTo().getEmployeeId(), Collectors.counting()));

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        Map<Long, Long> attendanceByEmployee = attendanceRepository
                .findByEmployeeEmployeeIdInAndDateBetween(employeeIds, start, end)
                .stream().collect(Collectors.groupingBy(a -> a.getEmployee().getEmployeeId(), Collectors.counting()));

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            log.info("OpenAI API key not configured. Using rule-based fallback scoring.");
            return calculateFallbackScores(request, employees, skillsByEmployee, activeTasksByEmployee, attendanceByEmployee);
        }
        String prompt = buildPrompt(request, employees, skillsByEmployee, activeTasksByEmployee, attendanceByEmployee);
        return callOpenAi(prompt, employees);
    }

    @Cacheable(value = "ai_suggestions", key = "'task-' + #taskId")
    public List<EmployeeSuggestionDTO> recommendEmployeesForTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        SuggestionRequest request = new SuggestionRequest();
        request.setTaskId(taskId);
        request.setTaskTitle(task.getTitle());
        request.setTaskDescription(task.getDescription());

        return recommendEmployees(request);
    }

    private List<EmployeeSuggestionDTO> calculateFallbackScores(SuggestionRequest request,
                                                                  List<Employee> employees,
                                                                  Map<Long, List<Skill>> skillsByEmployee,
                                                                  Map<Long, Long> activeTasksByEmployee,
                                                                  Map<Long, Long> attendanceByEmployee) {
        List<String> requiredSkills = request.getRequiredSkills();
        List<String> normalizedRequired = (requiredSkills != null && !requiredSkills.isEmpty())
                ? requiredSkills.stream().map(String::toLowerCase).collect(Collectors.toList())
                : List.of();

        List<EmployeeSuggestionDTO> results = new ArrayList<>();

        for (Employee emp : employees) {
            List<Skill> skills = skillsByEmployee.getOrDefault(emp.getEmployeeId(), List.of());
            long activeTasks = activeTasksByEmployee.getOrDefault(emp.getEmployeeId(), 0L);
            long attendanceDays = attendanceByEmployee.getOrDefault(emp.getEmployeeId(), 0L);

            // Skill Match Score (35%)
            double skillMatchScore;
            int matchedSkills = 0;
            if (normalizedRequired.isEmpty()) {
                skillMatchScore = DEFAULT_SKILL_MATCH_SCORE;
            } else {
                List<String> empSkillNames = skills.stream()
                        .map(s -> s.getSkillName().toLowerCase())
                        .collect(Collectors.toList());
                for (String req : normalizedRequired) {
                    if (empSkillNames.contains(req)) {
                        matchedSkills++;
                    }
                }
                skillMatchScore = (double) matchedSkills / normalizedRequired.size();
            }

            // Workload Score (25%)
            double workloadScore = Math.max(0.0, 1.0 - activeTasks * WORKLOAD_PENALTY_FACTOR);

            // Attendance Score (15%)
            double attendanceScore = Math.min(1.0, (double) attendanceDays / EXPECTED_WORKING_DAYS);

            // Performance Score (25%) — default neutral
            double performanceScore = DEFAULT_PERFORMANCE_SCORE;

            // Overall Score
            double overallScore = skillMatchScore * 0.35
                    + workloadScore * 0.25
                    + performanceScore * 0.25
                    + attendanceScore * 0.15;

            String reasoning = String.format(
                    "Rule-based scoring: %d/%d skills matched, %d active tasks, %d/%d attendance days",
                    matchedSkills,
                    normalizedRequired.isEmpty() ? 0 : normalizedRequired.size(),
                    activeTasks,
                    attendanceDays,
                    EXPECTED_WORKING_DAYS
            );

            results.add(new EmployeeSuggestionDTO(
                    emp.getEmployeeId(),
                    emp.getFirstName(),
                    emp.getLastName(),
                    emp.getDepartment(),
                    skillMatchScore,
                    workloadScore,
                    performanceScore,
                    attendanceScore,
                    overallScore,
                    reasoning
            ));
        }

        results.sort(Comparator.comparingDouble(EmployeeSuggestionDTO::getOverallScore).reversed());
        return results.stream().limit(TOP_N).collect(Collectors.toList());
    }

    private String buildPrompt(SuggestionRequest request, List<Employee> employees,
                                Map<Long, List<Skill>> skillsByEmployee,
                                Map<Long, Long> activeTasksByEmployee,
                                Map<Long, Long> attendanceByEmployee) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an AI assistant helping assign tasks to the most suitable employees in a task management system.\n\n");
        sb.append("Task Details:\n");
        sb.append("- Title: ").append(request.getTaskTitle()).append("\n");
        if (request.getTaskDescription() != null && !request.getTaskDescription().isBlank()) {
            sb.append("- Description: ").append(request.getTaskDescription()).append("\n");
        }
        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            sb.append("- Required Skills: ").append(String.join(", ", request.getRequiredSkills())).append("\n");
        }

        sb.append("\nAvailable Employees:\n");
        for (Employee emp : employees) {
            List<Skill> skills = skillsByEmployee.getOrDefault(emp.getEmployeeId(), List.of());
            long activeTasks = activeTasksByEmployee.getOrDefault(emp.getEmployeeId(), 0L);
            long attendance = attendanceByEmployee.getOrDefault(emp.getEmployeeId(), 0L);

            sb.append("- ID: ").append(emp.getEmployeeId())
              .append(", Name: ").append(emp.getFirstName()).append(" ").append(emp.getLastName())
              .append(", Department: ").append(emp.getDepartment())
              .append(", Position: ").append(emp.getPosition()).append("\n");
            if (!skills.isEmpty()) {
                String skillList = skills.stream()
                        .map(s -> s.getSkillName() + " (" + s.getProficiencyLevel() + ")")
                        .collect(Collectors.joining(", "));
                sb.append("  Skills: ").append(skillList).append("\n");
            }
            sb.append("  Active tasks: ").append(activeTasks).append("\n");
            sb.append("  Attendance last 30 days: ").append(attendance).append("/").append(EXPECTED_WORKING_DAYS).append(" days\n");
        }

        sb.append("\nRank the top ").append(TOP_N).append(" most suitable employees for this task.\n");
        sb.append("Consider: skill match, workload (fewer active tasks = more available), and attendance.\n");
        sb.append("Return ONLY a valid JSON array (no markdown, no extra text) with this structure:\n");
        sb.append("[{\"employeeId\":<number>,\"skillMatchScore\":<0.0-1.0>,\"workloadScore\":<0.0-1.0>,");
        sb.append("\"performanceScore\":<0.0-1.0>,\"attendanceScore\":<0.0-1.0>,");
        sb.append("\"overallScore\":<0.0-1.0>,\"reasoning\":\"<brief reason>\"}]");

        return sb.toString();
    }

    private List<EmployeeSuggestionDTO> callOpenAi(String prompt, List<Employee> employees) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured. Please set the openai.api.key property.");
        }

        Map<String, Object> requestBody = Map.of(
                "model", openAiModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3
        );

        String responseJson = restClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return parseOpenAiResponse(responseJson, employees);
    }

    private List<EmployeeSuggestionDTO> parseOpenAiResponse(String responseJson, List<Employee> employees) {
        String content = null;
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            content = root.get("choices").get(0).get("message").get("content").asText();

            content = content.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("(?s)\\s*```$", "");

            JsonNode results = objectMapper.readTree(content);

            Map<Long, Employee> empById = employees.stream()
                    .collect(Collectors.toMap(Employee::getEmployeeId, e -> e));

            List<EmployeeSuggestionDTO> dtos = new ArrayList<>();
            for (JsonNode node : results) {
                Long empId = node.path("employeeId").asLong(-1);
                Employee emp = empById.get(empId);
                if (emp == null) {
                    log.warn("AI returned unknown employeeId: {}. Skipping.", empId);
                    continue;
                }

                dtos.add(new EmployeeSuggestionDTO(
                        emp.getEmployeeId(),
                        emp.getFirstName(),
                        emp.getLastName(),
                        emp.getDepartment(),
                        node.path("skillMatchScore").asDouble(0.0),
                        node.path("workloadScore").asDouble(0.0),
                        node.path("performanceScore").asDouble(0.0),
                        node.path("attendanceScore").asDouble(0.0),
                        node.path("overallScore").asDouble(0.0),
                        node.path("reasoning").asText("")
                ));
            }
            return dtos;
        } catch (Exception e) {
            log.error("Failed to parse AI response. Content excerpt: {}. Error: {}",
                    content != null ? content.substring(0, Math.min(200, content.length())) : "N/A",
                    e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}
