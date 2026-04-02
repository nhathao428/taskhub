package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.AuthResponse;
import com.example.taskmanagement.dto.RegisterRequest;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.DuplicateResourceException;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    /** Register với thông tin hợp lệ → save() được gọi và password được encode */
    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse result = userService.register(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /** Register với username đã tồn tại → ném DuplicateResourceException */
    @Test
    void testRegister_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    /** Register với email đã tồn tại → ném DuplicateResourceException */
    @Test
    void testRegister_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    /** loadUserByUsername với username tồn tại → trả về UserDetails đúng */
    @Test
    void testLoadUserByUsername_Found() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole("EMPLOYEE");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    /** loadUserByUsername với username không tồn tại → ném UsernameNotFoundException */
    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent"));
    }
}
