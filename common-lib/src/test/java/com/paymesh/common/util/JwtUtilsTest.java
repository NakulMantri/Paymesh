package com.paymesh.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtils.generateToken(1L, "johndoe", "john@example.com", Set.of("ROLE_USER"));
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("johndoe", jwtUtils.extractUsername(token));
        assertEquals(1L, jwtUtils.extractUserId(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.token.payload"));
    }
}
