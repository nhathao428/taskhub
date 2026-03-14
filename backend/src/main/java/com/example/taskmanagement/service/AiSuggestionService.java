package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.EmployeeSuggestionDTO;
import com.example.taskmanagement.dto.SuggestionRequest;
import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.Skill;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.repository.AttendanceRepository;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.SkillRepository;
import com.example.taskmanagement.repository.TaskRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiSuggestionService {

    private static final int MAX_TASKS_THRESHOLD = 5;
    private static final int EXPECTED_WORKING_DAYS = 22;
    private static final int TOP_N = 5;

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;
    private final SkillRepository skillRepository;

    public AiSuggestionService(EmployeeRepository employeeRepository,
                                TaskRepository taskRepository,
                                AttendanceRepository attendanceRepository,
                                SkillRepository skillRepository) {
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
        this.attendanceRepository = attendanceRepository;
        this.skillRepository = skillRepository;
    }

    @Cacheable(value = "ai_suggestions", key = "#request.taskTitle + '-' + #request.requiredSkills")
    public List<EmployeeSuggestionDTO> recommendEmployees(SuggestionRequest request) {
        List<String> requiredSkills = buildRequiredSkills(request);
        List<Employee> employees = employeeRepository.findAll();

        List<EmployeeSuggestionDTO> suggestions = employees.stream()
                .map(employee -> scoreEmployee(employee, requiredSkills))
                .sorted(Comparator.comparingDouble(EmployeeSuggestionDTO::getOverallScore).reversed())
                .limit(TOP_N)
                .collect(Collectors.toList());

        return suggestions;
    }

    @Cacheable(value = "ai_suggestions", key = "'task-' + #taskId")
    public List<EmployeeSuggestionDTO> recommendEmployeesForTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        SuggestionRequest request = new SuggestionRequest();
        request.setTaskId(taskId);
        request.setTaskTitle(task.getTitle());
        request.setTaskDescription(task.getDescription());

        List<String> requiredSkills = buildRequiredSkills(request);
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .map(employee -> scoreEmployee(employee, requiredSkills))
                .sorted(Comparator.comparingDouble(EmployeeSuggestionDTO::getOverallScore).reversed())
                .limit(TOP_N)
                .collect(Collectors.toList());
    }

    private List<String> buildRequiredSkills(SuggestionRequest request) {
        List<String> skills = new ArrayList<>();
        if (request.getRequiredSkills() != null) {
            skills.addAll(request.getRequiredSkills());
        }
        if (request.getTaskTitle() != null) {
            skills.addAll(extractKeywords(request.getTaskTitle()));
        }
        if (request.getTaskDescription() != null) {
            skills.addAll(extractKeywords(request.getTaskDescription()));
        }
        return skills.stream().map(String::toLowerCase).distinct().collect(Collectors.toList());
    }

    private List<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        // Split on non-alphanumeric characters and filter out very short words
        return List.of(text.toLowerCase().split("[^a-z0-9]+"))
                .stream()
                .filter(w -> w.length() > 2)
                .collect(Collectors.toList());
    }

    private EmployeeSuggestionDTO scoreEmployee(Employee employee, List<String> requiredSkills) {
        double skillScore = calculateSkillScore(employee.getEmployeeId(), requiredSkills);
        double workloadScore = calculateWorkloadScore(employee.getEmployeeId());
        double performanceScore = calculatePerformanceScore(employee.getEmployeeId());
        double attendanceScore = calculateAttendanceScore(employee.getEmployeeId());

        double overallScore = (skillScore * 0.35)
                + (workloadScore * 0.25)
                + (performanceScore * 0.25)
                + (attendanceScore * 0.15);

        String reasoning = buildReasoning(skillScore, workloadScore, performanceScore, attendanceScore);

        return new EmployeeSuggestionDTO(
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDepartment(),
                skillScore,
                workloadScore,
                performanceScore,
                attendanceScore,
                overallScore,
                reasoning
        );
    }

    private double calculateSkillScore(Long employeeId, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 0.5; // neutral score when no skills specified
        }
        List<Skill> employeeSkills = skillRepository.findByEmployeeEmployeeId(employeeId);
        if (employeeSkills.isEmpty()) {
            return 0.0;
        }
        long matched = requiredSkills.stream()
                .filter(req -> employeeSkills.stream()
                        .anyMatch(s -> s.getSkillName().toLowerCase().contains(req)
                                || req.contains(s.getSkillName().toLowerCase())))
                .count();
        return (double) matched / requiredSkills.size();
    }

    private double calculateWorkloadScore(Long employeeId) {
        List<Task> activeTasks = taskRepository
                .findByAssignedToEmployeeIdAndStatusIn(employeeId, List.of("pending", "in_progress"));
        int currentTasks = activeTasks.size();
        double score = 1.0 - ((double) currentTasks / MAX_TASKS_THRESHOLD);
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculatePerformanceScore(Long employeeId) {
        List<Task> allTasks = taskRepository.findByAssignedToEmployeeId(employeeId);
        List<Task> completed = allTasks.stream()
                .filter(t -> "completed".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
        if (completed.isEmpty()) {
            return 0.5; // neutral score for new employees
        }
        // Count completed tasks whose due date is still in the future (completed before the deadline).
        // Since there is no completedAt field in the schema, a future dueDate on a completed task
        // is the best available indicator that the task was finished ahead of schedule.
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long withDeadline = completed.stream().filter(t -> t.getDueDate() != null).count();
        if (withDeadline == 0) {
            return 0.5; // no deadline info, return neutral
        }
        long onTime = completed.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(now))
                .count();
        return (double) onTime / withDeadline;
    }

    private double calculateAttendanceScore(Long employeeId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<Attendance> records = attendanceRepository
                .findByEmployeeEmployeeIdAndDateBetween(employeeId, start, end);
        double score = (double) records.size() / EXPECTED_WORKING_DAYS;
        return Math.min(1.0, score);
    }

    private String buildReasoning(double skillScore, double workloadScore,
                                   double performanceScore, double attendanceScore) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Skill match: %.0f%%. ", skillScore * 100));
        sb.append(String.format("Workload availability: %.0f%%. ", workloadScore * 100));
        sb.append(String.format("On-time performance: %.0f%%. ", performanceScore * 100));
        sb.append(String.format("Attendance (last 30 days): %.0f%%.", attendanceScore * 100));
        return sb.toString();
    }
}
