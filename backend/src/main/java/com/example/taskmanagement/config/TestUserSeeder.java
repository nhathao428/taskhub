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

/**
 * Seed sẵn các tài khoản test dùng để demo/kiểm thử: 1 MANAGER và 1 EMPLOYEE
 * (tài khoản ADMIN do AdminSeeder lo). Chạy khi khởi động, chỉ tạo nếu chưa có.
 * Tắt bằng app.seed.test-users=false (nên tắt ở môi trường production thật).
 */
@Component
public class TestUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestUserSeeder.class);

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.test-users:true}")
    private boolean seedTestUsers;

    public TestUserSeeder(UserRepository userRepository,
                          EmployeeRepository employeeRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedTestUsers) {
            return;
        }
        seedManager();
        seedEmployee();
    }

    private void seedManager() {
        String email = "manager@example.com";
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setUsername("manager");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Manager@123"));
        user.setRole("MANAGER");
        userRepository.save(user);
        log.info("Tài khoản test seeded: {} (MANAGER)", email);
    }

    private void seedEmployee() {
        String email = "employee@example.com";
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setUsername("employee");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Employee@123"));
        user.setRole("EMPLOYEE");
        User saved = userRepository.save(user);

        // Hồ sơ nhân viên liên kết tài khoản — để dùng được các màn hình tự phục vụ
        // (/api/employees/me, công việc của tôi, chấm công của tôi).
        Employee employee = new Employee();
        employee.setFirstName("Nhan vien");
        employee.setLastName("Demo");
        employee.setPosition("Nhan vien");
        employee.setDepartment("Ky thuat");
        employee.setSkills("Java, Spring Boot");
        employee.setUser(saved);
        employeeRepository.save(employee);
        log.info("Tài khoản test seeded: {} (EMPLOYEE, kèm hồ sơ nhân viên)", email);
    }
}
