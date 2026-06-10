package com.example.taskmanagement.config;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds một tài khoản EMPLOYEE mẫu khi khởi động, chỉ khi {@code EMPLOYEE_PASSWORD} được set.
 *
 * Cùng triết lý với {@link ManagerSeeder}: không có mật khẩu mặc định nên KHÔNG bật mặc định
 * (tránh tạo tài khoản backdoor mật khẩu yếu). Tạo cả hồ sơ Employee liên kết để các API
 * /me (employees/me, tasks/me, attendance/me) dùng được ngay.
 *
 * Cấu hình qua env: EMPLOYEE_USERNAME (mặc định "employee1"),
 * EMPLOYEE_EMAIL (mặc định "employee@example.com"), EMPLOYEE_PASSWORD (bắt buộc để seed).
 */
@Component
public class EmployeeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSeeder.class);

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.employee.username:employee1}")
    private String employeeUsername;

    @Value("${app.employee.email:employee@example.com}")
    private String employeeEmail;

    @Value("${app.employee.password:}")
    private String employeePassword;

    public EmployeeSeeder(UserRepository userRepository,
                          EmployeeRepository employeeRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (employeePassword == null || employeePassword.isBlank()) {
            log.info("EMPLOYEE_PASSWORD not set — skipping employee seed. "
                    + "Set EMPLOYEE_PASSWORD env var to seed a sample employee account.");
            return;
        }
        if (userRepository.existsByEmail(employeeEmail)) {
            log.info("Employee account already exists — skipping seed.");
            return;
        }

        User user = new User();
        user.setUsername(employeeUsername);
        user.setEmail(employeeEmail);
        user.setPassword(passwordEncoder.encode(employeePassword));
        user.setRole("EMPLOYEE");
        userRepository.save(user);

        // Hồ sơ Employee liên kết — cần cho các endpoint /me và để hiển thị trong danh sách nhân viên.
        Employee emp = new Employee();
        emp.setUser(user);
        emp.setFirstName("Nguyễn");
        emp.setLastName("Văn Nhân");
        emp.setPosition("Nhân viên");
        emp.setDepartment("Kỹ thuật");
        emp.setGroup("Nhóm Backend");
        emp.setSkills("Java, Spring Boot, PostgreSQL");
        employeeRepository.save(emp);

        log.info("Employee account seeded: username={}, email={}", employeeUsername, employeeEmail);
    }
}
