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
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtIssuer", "taskhub");
        jwtTokenProvider.init();
    }

    /** generateTokenFromUsername + parseUsername → token hợp lệ trả username. */
    @Test
    void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        assertNotNull(token);
        assertEquals("testuser", jwtTokenProvider.parseUsername(token));
    }

    /** parseUsername với chuỗi rác → null. */
    @Test
    void testParseUsername_InvalidToken() {
        assertNull(jwtTokenProvider.parseUsername("this.is.not.a.valid.token"));
    }

    /** parseUsername với token đã hết hạn → null. */
    @Test
    void testParseUsername_ExpiredToken() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);
        String expiredToken = jwtTokenProvider.generateTokenFromUsername("testuser");

        assertNull(jwtTokenProvider.parseUsername(expiredToken));
    }

    /** Secret ngắn hơn 32 byte → init() fail-fast (không chờ tới lần parse đầu). */
    @Test
    void testInit_RejectsShortSecret() {
        JwtTokenProvider p = new JwtTokenProvider();
        ReflectionTestUtils.setField(p, "jwtSecret", "too-short");
        ReflectionTestUtils.setField(p, "jwtExpirationMs", 86400000L);
        ReflectionTestUtils.setField(p, "jwtIssuer", "taskhub");
        assertThrows(IllegalStateException.class, p::init);
    }
}
