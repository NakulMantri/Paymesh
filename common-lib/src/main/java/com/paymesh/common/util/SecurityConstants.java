package com.paymesh.common.util;

public final class SecurityConstants {
    private SecurityConstants() {}

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    // Default secret for HMAC-SHA256 (256-bit key) - customizable via Config Server
    public static final String DEFAULT_JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    public static final long DEFAULT_JWT_EXPIRATION_MS = 86400000L; // 24 hours
}
