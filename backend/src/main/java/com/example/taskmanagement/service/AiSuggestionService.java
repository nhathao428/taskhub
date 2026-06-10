package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.EmployeeSuggestionDTO;
import com.example.taskmanagement.dto.SuggestionRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.exception.BusinessException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.AttendanceRepository;
import com.example.taskmanagement.repository.EmployeeRepository;
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
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Suggests the most suitable employees for a task using Google Gemini.
 * Backend collects raw historical data per employee (past task progress, completion timing,
 * attendance) and lets the AI rank and explain — no rule-based scoring.
 */
@Service
public class AiSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(AiSuggestionService.class);
    private static final int TOP_N = 5;
    private static final int ATTENDANCE_WINDOW_DAYS = 30;
    private static final int EXPECTED_WORKING_DAYS = 22;

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String chatCompletionsUrl;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String geminiModel;

    public AiSuggestionService(EmployeeRepository employeeRepository,
                               TaskRepository taskRepository,
                               AttendanceRepository attendanceRepository,
                               RestClient.Builder restClientBuilder,
                               ObjectMapper objectMapper,
                               @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta/openai/}") String baseUrl) {
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
        this.attendanceRepository = attendanceRepository;
        // RestClient.create() — fresh client, không phụ thuộc Spring-injected builder
        // (có thể đã set baseUrl prefix gây xung đột với absolute URI ta truyền vào).
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
        String normalized = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.chatCompletionsUrl = normalized + "chat/completions";
    }

    private static final int MAX_FIELD_LENGTH = 500;

    // PromptShield-style: regex phát hiện các cấu trúc prompt-injection phổ biến.
    // Match → reject sớm ở 422 thay vì gửi sang Gemini (tốn quota + risk leak).
    private static final java.util.regex.Pattern INJECTION_PATTERNS = java.util.regex.Pattern.compile(
            "(?i)("
            + "ignore\\s+(all\\s+)?previous\\s+instructions"
            + "|disregard\\s+(all\\s+)?(prior|above|previous)"
            + "|forget\\s+everything"
            + "|you\\s+are\\s+now\\s+(in\\s+)?(admin|root|system|dev|developer|jailbreak)"
            + "|reveal\\s+(your\\s+)?(system\\s+)?prompt"
            + "|<\\|im_(start|end)\\|>"
            + "|<\\|endoftext\\|>"
            + "|\\[\\[\\s*system\\s*\\]\\]"
            + "|bỏ\\s+qua\\s+(các\\s+)?hướng\\s+dẫn"
            + "|quên\\s+(tất\\s+cả|mọi\\s+thứ)"
            + ")");

    // PII redaction trước khi gửi prompt: email, credit card, SSN, VN phone, VN CCCD.
    private static final java.util.regex.Pattern EMAIL = java.util.regex.Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final java.util.regex.Pattern CREDIT_CARD = java.util.regex.Pattern.compile(
            "\\b(?:\\d[ -]?){13,19}\\b");
    private static final java.util.regex.Pattern VN_PHONE = java.util.regex.Pattern.compile(
            "(?:\\+84|0)(?:3|5|7|8|9)\\d{8}");
    private static final java.util.regex.Pattern VN_CCCD = java.util.regex.Pattern.compile(
            "\\b\\d{12}\\b");

    @Cacheable(value = "ai_suggestions", key = "#request.cacheKey")
    public List<EmployeeSuggestionDTO> recommendEmployees(SuggestionRequest request) {
        if (request == null || request.getTaskTitle() == null || request.getTaskTitle().isBlank()) {
            throw new IllegalArgumentException("Task title is required");
        }
        requireApiKey();

        // Security H3: Sanitize → reject-if-injection (PromptShield pattern) →
        // forward. Sanitize trước để injection detect không trigger trên control
        // char giả; reject trên text đã clean.
        request.setTaskTitle(sanitizePromptInput(request.getTaskTitle()));
        request.setTaskDescription(sanitizePromptInput(request.getTaskDescription()));
        request.setRequiredSkills(sanitizePromptInput(request.getRequiredSkills()));
        rejectIfInjectionDetected("tiêu đề", request.getTaskTitle());
        rejectIfInjectionDetected("mô tả", request.getTaskDescription());
        rejectIfInjectionDetected("kỹ năng yêu cầu", request.getRequiredSkills());

        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            return List.of();
        }

        Map<Long, EmployeeStats> statsByEmployee = collectStats(employees);
        String prompt = buildPrompt(request, employees, statsByEmployee);
        return callGemini(prompt, employees);
    }

    @Cacheable(value = "ai_suggestions", key = "'task-' + #taskId")
    public List<EmployeeSuggestionDTO> recommendEmployeesForTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        SuggestionRequest request = new SuggestionRequest();
        request.setTaskId(taskId);
        request.setTaskTitle(task.getTitle());
        request.setTaskDescription(task.getDescription());
        request.setRequiredSkills(task.getRequiredSkills());

        return recommendEmployees(request);
    }

    private void requireApiKey() {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new BusinessException("AI suggestion is unavailable: GEMINI_API_KEY is not configured");
        }
    }

    /**
     * Loại bỏ control chars (newline/tab → space), section markers (===), backticks và
     * trim độ dài. Giảm bề mặt prompt injection cho user-controlled fields trước khi
     * concat vào prompt Gemini.
     */
    static String sanitizePromptInput(String raw) {
        if (raw == null) return null;
        // Newline/tab/CR → space; xoá control chars khác
        String stripped = raw.replaceAll("[\\r\\n\\t]+", " ")
                             .replaceAll("\\p{Cntrl}", "");
        // Block các marker dễ bị abuse để giả lập section của prompt
        stripped = stripped.replace("===", "≡≡≡")
                           .replace("```", "ʼʼʼ");
        // PII redaction (PromptShield-style): mask trước khi nhồi vào prompt — kể cả
        // không có gateway, app vẫn không tự leak PII của user lên LLM bên thứ ba.
        stripped = EMAIL.matcher(stripped).replaceAll("[EMAIL]");
        stripped = CREDIT_CARD.matcher(stripped).replaceAll("[CARD]");
        stripped = VN_PHONE.matcher(stripped).replaceAll("[PHONE]");
        stripped = VN_CCCD.matcher(stripped).replaceAll("[ID]");
        // Trim trắng + giới hạn độ dài để cap input
        stripped = stripped.trim();
        if (stripped.length() > MAX_FIELD_LENGTH) {
            stripped = stripped.substring(0, MAX_FIELD_LENGTH) + "…";
        }
        return stripped;
    }

    /**
     * Detect prompt injection patterns sau khi sanitize. Trả về BusinessException
     * (422) thay vì im lặng forward → tiết kiệm quota Gemini + audit logable.
     */
    static void rejectIfInjectionDetected(String field, String value) {
        if (value == null) return;
        if (INJECTION_PATTERNS.matcher(value).find()) {
            throw new BusinessException(
                    "Nội dung " + field + " chứa pattern không cho phép, vui lòng diễn đạt lại.");
        }
    }

    /** Aggregates per-employee historical stats: task progress, completion timing, attendance. */
    private Map<Long, EmployeeStats> collectStats(List<Employee> employees) {
        List<Long> employeeIds = employees.stream().map(Employee::getEmployeeId).toList();

        Map<Long, List<Task>> tasksByEmployee = taskRepository.findByAssignedToEmployeeIdIn(employeeIds)
                .stream()
                .filter(t -> t.getAssignedTo() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignedTo().getEmployeeId()));

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(ATTENDANCE_WINDOW_DAYS);
        Map<Long, Long> attendanceDays = attendanceRepository
                .findByEmployeeEmployeeIdInAndDateBetween(employeeIds, start, end)
                .stream()
                .filter(a -> a.getEmployee() != null)
                .collect(Collectors.groupingBy(a -> a.getEmployee().getEmployeeId(), Collectors.counting()));

        Map<Long, EmployeeStats> result = new HashMap<>();
        for (Long empId : employeeIds) {
            List<Task> tasks = tasksByEmployee.getOrDefault(empId, List.of());
            EmployeeStats stats = new EmployeeStats();
            stats.totalTasks = tasks.size();
            stats.activeTasks = (int) tasks.stream()
                    .filter(t -> isActive(t.getStatus()))
                    .count();
            stats.completedTasks = (int) tasks.stream()
                    .filter(t -> isCompleted(t.getStatus()))
                    .count();

            List<Task> finishedWithDue = tasks.stream()
                    .filter(t -> isCompleted(t.getStatus())
                            && t.getDueDate() != null
                            && t.getCompletedAt() != null)
                    .toList();
            stats.completedWithDueDate = finishedWithDue.size();
            stats.completedOnTime = (int) finishedWithDue.stream()
                    .filter(t -> !t.getCompletedAt().toLocalDate().isAfter(t.getDueDate()))
                    .count();
            stats.avgDaysLate = finishedWithDue.stream()
                    .mapToLong(t -> Math.max(0, ChronoUnit.DAYS.between(
                            t.getDueDate(), t.getCompletedAt().toLocalDate())))
                    .average()
                    .orElse(0.0);

            stats.attendanceDays = attendanceDays.getOrDefault(empId, 0L).intValue();
            result.put(empId, stats);
        }
        return result;
    }

    private boolean isActive(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.equals("pending") || s.equals("in_progress");
    }

    private boolean isCompleted(String status) {
        return status != null && status.toLowerCase().equals("completed");
    }

    private String buildPrompt(SuggestionRequest request, List<Employee> employees,
                               Map<Long, EmployeeStats> statsByEmployee) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý AI giúp quản lý chọn nhân viên phù hợp nhất để giao một task. ")
          .append("Dữ liệu có thể bằng tiếng Việt hoặc tiếng Anh.\n\n");

        sb.append("=== TASK CẦN GIAO ===\n");
        sb.append("- Tiêu đề: ").append(request.getTaskTitle()).append("\n");
        if (request.getTaskDescription() != null && !request.getTaskDescription().isBlank()) {
            sb.append("- Mô tả: ").append(request.getTaskDescription()).append("\n");
        }
        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isBlank()) {
            sb.append("- Kỹ năng yêu cầu: ").append(request.getRequiredSkills()).append("\n");
        }

        sb.append("\n=== DỮ LIỆU LỊCH SỬ CỦA TỪNG NHÂN VIÊN ===\n");
        sb.append("(số liệu thô — KHÔNG được tính điểm/score, hãy đánh giá định tính)\n\n");
        for (Employee emp : employees) {
            EmployeeStats stats = statsByEmployee.getOrDefault(emp.getEmployeeId(), new EmployeeStats());
            sb.append("• ID=").append(emp.getEmployeeId())
              .append(" | ").append((emp.getFirstName() + " " + emp.getLastName()).trim())
              .append(" | ").append(emp.getDepartment() != null ? emp.getDepartment() : "—")
              .append(" | ").append(emp.getPosition() != null ? emp.getPosition() : "—").append("\n");
            if (emp.getSkills() != null && !emp.getSkills().isBlank()) {
                sb.append("    - Kỹ năng: ").append(emp.getSkills()).append("\n");
            }
            sb.append("    - Tiến độ task trước: ")
              .append(stats.completedTasks).append(" hoàn thành / ")
              .append(stats.totalTasks).append(" tổng task được giao")
              .append(" (").append(stats.activeTasks).append(" đang xử lý)\n");
            if (stats.completedWithDueDate > 0) {
                sb.append("    - Thời gian hoàn thành: ")
                  .append(stats.completedOnTime).append("/").append(stats.completedWithDueDate)
                  .append(" task hoàn thành đúng hạn");
                if (stats.avgDaysLate > 0) {
                    sb.append(", trung bình trễ ").append(String.format("%.1f", stats.avgDaysLate)).append(" ngày");
                }
                sb.append("\n");
            } else {
                sb.append("    - Thời gian hoàn thành: chưa có task hoàn thành nào có due date\n");
            }
            sb.append("    - Chấm công ").append(ATTENDANCE_WINDOW_DAYS).append(" ngày qua: ")
              .append(stats.attendanceDays).append("/").append(EXPECTED_WORKING_DAYS).append(" ngày làm việc\n");
        }

        sb.append("\n=== HƯỚNG DẪN ===\n");
        sb.append("Hãy gợi ý TOP ").append(TOP_N).append(" nhân viên phù hợp nhất với task trên, dựa trên các tiêu chí (theo thứ tự ưu tiên):\n");
        sb.append("  1. Mức độ phù hợp về KỸ NĂNG & CHUYÊN MÔN — đối chiếu kỹ năng yêu cầu của task ")
          .append("với kỹ năng nhân viên đã liệt kê (nếu có), kết hợp chức danh (position) và phòng ban (department).\n");
        sb.append("     • Nếu task không liệt kê kỹ năng yêu cầu hoặc mô tả chung chung, hãy SUY LUẬN từ tiêu đề + ")
          .append("chức danh + phòng ban + kỹ năng employee để chọn vị trí phù hợp nhất (vd: task giao diện → Frontend/UI; ")
          .append("CSDL → Backend/DBA; tuyển dụng → HR; báo cáo tài chính → Kế toán...).\n");
        sb.append("     • Trong lý do PHẢI nêu rõ kỹ năng/chức danh/phòng ban của họ khớp với task ra sao.\n");
        sb.append("  2. Tiến độ task trước — tỷ lệ hoàn thành cao, ít task tồn đọng.\n");
        sb.append("  3. Thời gian hoàn thành task trước — hoàn thành đúng hạn nhiều, ít trễ.\n");
        sb.append("  4. Chấm công — đi làm đầy đủ.\n\n");
        sb.append("KHÔNG cần tính điểm số. Hãy đánh giá định tính, so sánh giữa các nhân viên dựa trên dữ liệu trên ")
          .append("rồi xếp hạng. Nhân viên chưa có lịch sử thì xét chuyên môn + workload hiện tại + chấm công.\n\n");

        sb.append("=== ĐỊNH DẠNG TRẢ VỀ ===\n");
        sb.append("Trả về DUY NHẤT một mảng JSON hợp lệ (không kèm markdown, không text thừa), ")
          .append("đã sắp xếp theo độ phù hợp giảm dần:\n");
        sb.append("[{\"employeeId\":<số>,\"rank\":<1.." ).append(TOP_N).append(">,")
          .append("\"reasoning\":\"<lý do ngắn gọn bằng tiếng Việt: nêu rõ (a) kỹ năng/chức danh/phòng ban phù hợp với task ra sao, ")
          .append("(b) tiến độ, đúng hạn, chấm công của họ>\"}]");

        return sb.toString();
    }

    private List<EmployeeSuggestionDTO> callGemini(String prompt, List<Employee> employees) {
        // OpenAI-format request — tương thích cả PromptShield gateway (/v1/chat/completions)
        // lẫn Gemini OpenAI-compat (/v1beta/openai/chat/completions). Base URL controls
        // destination; gateway sẽ enforce policy (PII block/mask) trước khi forward.
        Map<String, Object> requestBody = Map.of(
                "model", geminiModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3,
                "response_format", Map.of("type", "json_object")
        );

        String responseJson;
        try {
            log.info("Calling AI endpoint: {}", chatCompletionsUrl);
            responseJson = restClient.post()
                    .uri(java.net.URI.create(chatCompletionsUrl))
                    .header("Authorization", "Bearer " + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            log.error("AI gateway/provider trả lỗi {}: {}", statusCode, e.getResponseBodyAsString());
            throw new BusinessException(switch (statusCode) {
                // 403 từ PromptShield gateway nghĩa là policy block (PII/injection).
                case 403 -> "Nội dung gửi AI bị chặn bởi policy bảo mật.";
                case 429 -> "AI tạm thời hết hạn mức gọi (rate limit), vui lòng thử lại sau ít phút.";
                case 503 -> "Dịch vụ AI đang quá tải, vui lòng thử lại sau ít phút.";
                case 400, 401 -> "Cấu hình AI không hợp lệ — kiểm tra lại GEMINI_API_KEY và model.";
                default -> "Gọi dịch vụ AI thất bại (mã " + statusCode + "), vui lòng thử lại sau.";
            });
        }

        return parseOpenAiResponse(responseJson, employees);
    }

    private List<EmployeeSuggestionDTO> parseOpenAiResponse(String responseJson, List<Employee> employees) {
        String content = null;
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new BusinessException("AI không trả về kết quả gợi ý");
            }
            content = choices.get(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                String finishReason = choices.get(0).path("finish_reason").asText("");
                throw new BusinessException("AI trả về nội dung rỗng"
                        + (finishReason.isBlank() ? "" : " (finish_reason: " + finishReason + ")"));
            }

            content = content.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("(?s)\\s*```$", "");

            JsonNode results = objectMapper.readTree(content);

            Map<Long, Employee> empById = employees.stream()
                    .collect(Collectors.toMap(Employee::getEmployeeId, e -> e));

            List<EmployeeSuggestionDTO> dtos = new ArrayList<>();
            int fallbackRank = 1;
            for (JsonNode node : results) {
                Long empId = node.path("employeeId").asLong(-1);
                Employee emp = empById.get(empId);
                if (emp == null) {
                    log.warn("AI returned unknown employeeId: {}. Skipping.", empId);
                    continue;
                }
                int rank = node.path("rank").asInt(fallbackRank);
                dtos.add(new EmployeeSuggestionDTO(
                        emp.getEmployeeId(),
                        emp.getFirstName(),
                        emp.getLastName(),
                        emp.getDepartment(),
                        rank,
                        node.path("reasoning").asText("")
                ));
                fallbackRank++;
            }
            return dtos;
        } catch (BusinessException e) {
            log.warn("Gemini did not return a usable suggestion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse AI response. Content excerpt: {}. Error: {}",
                    content != null ? content.substring(0, Math.min(200, content.length())) : "N/A",
                    e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private static class EmployeeStats {
        int totalTasks;
        int activeTasks;
        int completedTasks;
        int completedWithDueDate;
        int completedOnTime;
        double avgDaysLate;
        int attendanceDays;
    }
}
