package com.fintech.loan.service;

import com.fintech.loan.dto.AuditLogDTO;
import com.fintech.loan.entity.AuditLog;
import com.fintech.loan.repository.AuditLogRepository;
import com.fintech.loan.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for audit logging and compliance.
 *
 * <p>Provides read-only access to the audit trail for all entity changes
 * in the system for compliance and debugging purposes.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Logs an audit event to the audit repository.
     *
     * @param entityType the type of entity being audited ( e.g., APPLICANT, LOAN_APPLICATION)
     * @param entityId the entity ID
     * @param action the action type (e.g., CREATE, UPDATE, DELETE)
     * @param oldValue the old value for audit
     * @param newValue the new value for audit
     * @param actor the user who performed the action
     * @param ipAddress the IP address of the action
     */
    @Transactional
    public void log(String entityType, Long entityId, String action, Object oldValue, Object newValue, String actor, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .actor(actor)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} {} by {}", entityType, action, entityId, actor);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log values: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AuditLogDTO.ListResponse queryAuditLogs(
            String entityType, Long entityId, String action, String actor,
            Instant start, Instant end, int page, int size) {

        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        if (page < Constants.MIN_PAGE_NUMBER) page = Constants.MIN_PAGE_NUMBER;
        if (safeSize < 1) safeSize = Constants.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = auditLogRepository.findByFilters(entityType, entityId, action, actor, start, end, pageable);

        List<AuditLogDTO.Response> content = logs.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return AuditLogDTO.ListResponse.builder()
                .content(content)
                .pageNumber(logs.getNumber())
                .pageSize(logs.getSize())
                .totalElements(logs.getTotalElements())
                .totalPages(logs.getTotalPages())
                .last(logs.isLast())
                .build();
    }

    /**
     * Retrieves audit history for a specific entity with pagination.
     *
     * @param entityType the entity type (e.g., APPLICANT, LOAN_APPLICATION)
     * @param entityId the entity ID
     * @param page the page number (0-indexed)
     * @param size the page size (max 100)
     * @return paginated list of audit log entries
     */
    @Transactional(readOnly = true)
    public AuditLogDTO.ListResponse getEntityHistory(String entityType, Long entityId, int page, int size) {
        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        if (page < Constants.MIN_PAGE_NUMBER) page = Constants.MIN_PAGE_NUMBER;
        if (safeSize < 1) safeSize = Constants.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);

        List<AuditLogDTO.Response> content = logs.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return AuditLogDTO.ListResponse.builder()
                .content(content)
                .pageNumber(logs.getNumber())
                .pageSize(logs.getSize())
                .totalElements(logs.getTotalElements())
                .totalPages(logs.getTotalPages())
                .last(logs.isLast())
                .build();
    }

    private AuditLogDTO.Response toResponse(AuditLog auditLog) {
        return AuditLogDTO.Response.builder()
                .id(auditLog.getId())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .actor(auditLog.getActor())
                .ipAddress(auditLog.getIpAddress())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
