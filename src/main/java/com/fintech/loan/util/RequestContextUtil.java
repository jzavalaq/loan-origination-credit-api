package com.fintech.loan.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility component for extracting request context information.
 * Provides common methods for getting client IP and current actor.
 */
@Component
public class RequestContextUtil {

    private static final String UNKNOWN_ACTOR = "unknown";

    /**
     * Gets the current actor from security context.
     * In a real application, this would extract from Spring Security.
     *
     * @return the current actor identifier
     */
    public String getCurrentActor() {
        // In a real application, this would get the current user from SecurityContext
        // Example: SecurityContextHolder.getContext().getAuthentication().getName()
        return Constants.SYSTEM_ACTOR;
    }

    /**
     * Extracts the client IP address from the current request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @return the client IP address, or null if no request context is available
     */
    public String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return extractClientIp(request);
    }

    /**
     * Extracts the client IP address from the given request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    public String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(Constants.HEADER_X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Gets the current HTTP servlet request from the request context holder.
     *
     * @return the current request, or null if not available
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }
}
