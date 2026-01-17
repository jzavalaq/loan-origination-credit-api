package com.openclaw.loanorigination.controller;

import com.openclaw.loanorigination.dto.AuditLogDTO;
import com.openclaw.loanorigination.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

/**
 * REST controller for querying audit logs.
 *
 * <p>Provides read-only access to the audit trail of all entity changes
 * in the system for compliance and debugging purposes.</p>
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Operations for querying audit trail records")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Queries audit logs with flexible filtering options.
     *
     * @param entityType optional filter by entity type (e.g., APPLICANT, LOAN_APPLICATION)
     * @param entityId optional filter by entity ID
     * @param action optional filter by action type (e.g., CREATE, UPDATE, DELETE)
     * @param actor optional filter by the user who performed the action
     * @param start optional filter by start timestamp
     * @param end optional filter by end timestamp
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated list of matching audit log entries
     */
    @Operation(summary = "Query audit logs", description = "Retrieves a paginated list of audit log entries with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<AuditLogDTO.ListResponse> queryAuditLogs(
            @Parameter(description = "Filter by entity type (APPLICANT, LOAN_APPLICATION, CREDIT_ASSESSMENT)")
            @RequestParam(required = false) String entityType,
            @Parameter(description = "Filter by entity ID") @RequestParam(required = false) Long entityId,
            @Parameter(description = "Filter by action (CREATE, UPDATE, DELETE, SUBMIT, ASSESS, etc.)")
            @RequestParam(required = false) String action,
            @Parameter(description = "Filter by actor (user who performed the action)")
            @RequestParam(required = false) String actor,
            @Parameter(description = "Filter by start timestamp (ISO 8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "Filter by end timestamp (ISO 8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("Querying audit logs: entityType={}, entityId={}, action={}, actor={}, start={}, end={}, page={}, size={}",
                entityType, entityId, action, actor, start, end, page, size);
        AuditLogDTO.ListResponse response = auditLogService.queryAuditLogs(entityType, entityId, action, actor, start, end, page, size);
        return ResponseEntity.ok(response);
    }
}
