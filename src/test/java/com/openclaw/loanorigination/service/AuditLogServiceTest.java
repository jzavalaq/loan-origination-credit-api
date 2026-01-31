package com.openclaw.loanorigination.service;

import com.openclaw.loanorigination.dto.AuditLogDTO;
import com.openclaw.loanorigination.entity.AuditLog;
import com.openclaw.loanorigination.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() throws Exception {
        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setEntityType("APPLICANT");
        testAuditLog.setEntityId(1L);
        testAuditLog.setAction("CREATE");
        testAuditLog.setActor("system");
        testAuditLog.setTimestamp(Instant.now());

        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("serialized");
    }

    @Test
    @DisplayName("log - creates audit log entry")
    void log_createsAuditLogEntry() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        auditLogService.log("APPLICANT", 1L, "CREATE", null, "newValue", "system", "127.0.0.1");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("queryAuditLogs - returns paginated audit logs")
    void queryAuditLogs_returnsPaginatedAuditLogs() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(testAuditLog), pageable, 1);

        when(auditLogRepository.findByFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(auditLogPage);

        AuditLogDTO.ListResponse response = auditLogService.queryAuditLogs("APPLICANT", null, null, null, null, null, 0, 20);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }

    @Test
    @DisplayName("queryAuditLogs - with entity type filter - returns filtered results")
    void queryAuditLogsWithEntityTypeFilter_returnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(testAuditLog), pageable, 1);

        when(auditLogRepository.findByFilters(eq("APPLICANT"), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(auditLogPage);

        AuditLogDTO.ListResponse response = auditLogService.queryAuditLogs("APPLICANT", null, null, null, null, null, 0, 20);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("APPLICANT", response.getContent().get(0).getEntityType());
    }

    @Test
    @DisplayName("getEntityHistory - returns entity history")
    void getEntityHistory_returnsEntityHistory() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(testAuditLog), pageable, 1);

        when(auditLogRepository.findByEntityTypeAndEntityId("APPLICANT", 1L, pageable)).thenReturn(auditLogPage);

        AuditLogDTO.ListResponse response = auditLogService.getEntityHistory("APPLICANT", 1L, 0, 20);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }
}
