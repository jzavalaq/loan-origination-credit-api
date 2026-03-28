package com.fintech.loan.config;

import com.fintech.loan.util.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Request logging filter that adds correlation IDs and logs request/response details.
 *
 * <p>Generates or propagates X-Correlation-ID headers for request tracing
 * and logs request method, URI, actor, and client IP for debugging.</p>
 */
@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String UNKNOWN = "unknown";

    /**
     * Filters requests to add correlation ID and log request details.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = getCorrelationId(request);
        response.setHeader(Constants.HEADER_X_CORRELATION_ID, correlationId);

        String actor = getActor(request);
        String clientIp = getClientIp(request);

        log.debug("Request: method={}, uri={}, actor={}, clientIp={}, correlationId={}",
            request.getMethod(), request.getRequestURI(), actor, clientIp, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.debug("Response: status={}, correlationId={}", response.getStatus(), correlationId);
        }
    }

    /**
     * Gets or generates a correlation ID for request tracing.
     *
     * @param request the HTTP request
     * @return the correlation ID
     */
    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(Constants.HEADER_X_CORRELATION_ID);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    /**
     * Extracts the actor from request headers.
     *
     * @param request the HTTP request
     * @return the actor identifier
     */
    private String getActor(HttpServletRequest request) {
        String actor = request.getHeader("X-Actor");
        if (actor == null || actor.isEmpty()) {
            actor = Constants.SYSTEM_ACTOR;
        }
        return actor;
    }

    /**
     * Extracts the client IP address from the request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(Constants.HEADER_X_FORWARDED_FOR);
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple proxies - take the first IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
