package com.edu.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
            "edu-platform-jwt-secret-key-2026-very-long-string-for-security");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(1L, 100L, "admin");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateToken(1L, 100L, "admin");
        Long userId = jwtUtil.getUserIdFromToken(token);
        assertEquals(1L, userId);
    }

    @Test
    void testGetTenantIdFromToken() {
        String token = jwtUtil.generateToken(1L, 100L, "admin");
        Long tenantId = jwtUtil.getTenantIdFromToken(token);
        assertEquals(100L, tenantId);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtUtil.generateToken(1L, 100L, "admin");
        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("admin", username);
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid-token"));
    }
}
