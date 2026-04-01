package com.example.taskmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET =
            "testSecretKeyThatIsAtLeast256BitsLongForUnitTesting1234567890";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L);
    }

    /** generateTokenFromUsername + validateToken → token hợp lệ trả true */
    @Test
    void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    /** generateTokenFromUsername + getUsernameFromToken → trả đúng username */
    @Test
    void testGetUsernameFromToken() {
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    /** validateToken với chuỗi rác → trả false */
    @Test
    void testValidateToken_InvalidToken() {
        boolean valid = jwtTokenProvider.validateToken("this.is.not.a.valid.token");

        assertFalse(valid);
    }

    /** validateToken với token đã hết hạn → trả false */
    @Test
    void testValidateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);
        String expiredToken = jwtTokenProvider.generateTokenFromUsername("testuser");

        boolean valid = jwtTokenProvider.validateToken(expiredToken);

        assertFalse(valid);
    }
}
