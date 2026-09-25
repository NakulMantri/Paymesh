package com.paymesh.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.util.JwtUtils;
import com.paymesh.common.util.SecurityConstants;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<String> openEndpoints = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/actuator",
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus",
            "/fallback"
    );

    public JwtAuthenticationFilter(
            @Value("${paymesh.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String jwtSecret,
            @Value("${paymesh.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.jwtUtils = new JwtUtils(jwtSecret, jwtExpirationMs);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Attach correlation ID if not present
        String correlationId = request.getHeaders().getFirst(SecurityConstants.HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Allow open endpoints without auth
        if (isOpenEndpoint(path)) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(SecurityConstants.HEADER_CORRELATION_ID, correlationId)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // Verify Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
        if (!jwtUtils.validateToken(token)) {
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        Claims claims = jwtUtils.extractClaims(token);
        String username = claims.getSubject();
        Object userIdObj = claims.get("userId");
        String userId = userIdObj != null ? String.valueOf(userIdObj) : "";
        Object rolesObj = claims.get("roles");
        String roles = rolesObj != null ? rolesObj.toString() : "";

        // Mutate request with user claims for downstream microservices
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(SecurityConstants.HEADER_USER_ID, userId)
                .header(SecurityConstants.HEADER_USER_NAME, username)
                .header(SecurityConstants.HEADER_USER_ROLES, roles)
                .header(SecurityConstants.HEADER_CORRELATION_ID, correlationId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isOpenEndpoint(String path) {
        return openEndpoints.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> apiResponse = ApiResponse.failure(err, "UNAUTHORIZED");
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(apiResponse).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = ("{\"success\":false,\"message\":\"" + err + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Run early before routing
    }
}
