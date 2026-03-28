package com.fintech.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Objects for AuditLog operations.
 *
 * <p>Contains response DTOs for audit log data.</p>
 */
public class AuditLogDTO {

    /**
     * Response DTO for audit log entry data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String entityType;
        private Long entityId;
        private String action;
        private String oldValue;
        private String newValue;
        private String actor;
        private String ipAddress;
        private Instant timestamp;
    }

    /**
     * Paginated list response DTO for audit logs.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private List<Response> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
