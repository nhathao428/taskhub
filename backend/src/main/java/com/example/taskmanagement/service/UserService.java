package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.LoginRequest;
import com.example.taskmanagement.dto.RegisterRequest;
import com.example.taskmanagement.dto.AuthResponse;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.JwtTokenProvider;
import com.example.taskmanagement.security.LoginAttemptService;
import com.example.taskmanagement.exception.BusinessException;
import com.example.taskmanagement.exception.DuplicateResourceException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;

    public UserService(UserRepository userRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    @Cacheable(value = "user_details", key = "#username", unless = "#result == null")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Security M7: Cache 60s (xem RedisConfig) để JwtAuthenticationFilter không hit DB
        // mỗi request. Trade-off: role/delete propagate trong 60s — đủ nhanh cho hầu hết
        // trường hợp + giảm vector DoS amplification.
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                // Security M3: message generic, KHÔNG kèm username để tránh leak khi exception
                // được log lại bởi handler khác. Spring vẫn translate sang 401 đầy đủ.
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    public AuthResponse login(LoginRequest request) {
        // Security M4: Check lockout TRƯỚC khi gọi authenticationManager
        // (tránh bcrypt cost waste + chống brute-force phân tán).
        if (loginAttemptService.isLocked(request.getEmail())) {
            throw new BusinessException(
                    "Tài khoản tạm thời bị khoá do nhiều lần đăng nhập sai. Vui lòng thử lại sau ít phút.");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            String token = jwtTokenProvider.generateToken(authentication);
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            loginAttemptService.recordSuccess(request.getEmail());
            return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
        } catch (RuntimeException ex) {
            loginAttemptService.recordFailure(request.getEmail());
            throw ex;
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Anti-enumeration: trả message generic cho cả username + email collision.
        // Chi tiết được log server-side (xem warn log trong logger) — KHÔNG echo về client
        // để attacker không brute-force enumerate user database.
        boolean usernameExists = userRepository.existsByUsername(request.getUsername());
        boolean emailExists = userRepository.existsByEmail(request.getEmail());
        if (usernameExists || emailExists) {
            org.slf4j.LoggerFactory.getLogger(UserService.class).warn(
                    "Registration collision (anti-enumeration generic 409): usernameTaken={}, emailTaken={}",
                    usernameExists, emailExists);
            throw new DuplicateResourceException("Account", "credentials",
                    "đã tồn tại — kiểm tra lại thông tin đăng ký");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Người tự đăng ký luôn là EMPLOYEE; ADMIN sẽ nâng lên MANAGER khi cần.
        user.setRole("EMPLOYEE");
        userRepository.save(user);

        // Tự tạo hồ sơ Employee liên kết để tài khoản nhân viên dùng được ngay
        // (các API /me cần Employee). Quản lý có thể bổ sung phòng ban/nhóm/kỹ năng sau.
        // Cùng @Transactional với việc tạo User -> tạo cả 2 nguyên tử.
        Employee employee = new Employee();
        employee.setUser(user);
        // Tách "họ tên" từ username nếu có khoảng trắng; nếu chỉ 1 từ thì lastName = ""
        // (cột NOT NULL). Hiển thị tên đã trim nên không dư khoảng trắng. Manager bổ sung sau.
        String fullName = request.getUsername().trim();
        int sep = fullName.indexOf(' ');
        if (sep > 0) {
            employee.setFirstName(fullName.substring(0, sep).trim());
            employee.setLastName(fullName.substring(sep + 1).trim());
        } else {
            employee.setFirstName(fullName);
            employee.setLastName("");
        }
        employeeRepository.save(employee);

        return new AuthResponse(null, user.getUsername(), user.getEmail(), user.getRole());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** ADMIN đổi vai trò một user — chỉ giữa MANAGER và EMPLOYEE. */
    @Transactional
    @CacheEvict(value = "user_details", allEntries = true)
    public User updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("Không thể đổi vai trò của tài khoản ADMIN");
        }
        String role = newRole == null ? "" : newRole.trim().toUpperCase();
        if (!role.equals("MANAGER") && !role.equals("EMPLOYEE")) {
            throw new BusinessException("Vai trò chỉ được là MANAGER hoặc EMPLOYEE");
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "user_details", allEntries = true)
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        user.setPassword(passwordEncoder.encode("Reset@123"));
        userRepository.save(user);
    }
}
