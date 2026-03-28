package com.fintech.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Entity representing an audit log entry.
 *
 * <p>Stores immutable audit records for all entity changes in the system
 * for compliance and debugging purposes.</p>
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_actor", columnList = "actor")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String action;

    @Column(length = 2000)
    private String oldValue;

    @Column(nullable = false, length = 2000)
    private String newValue;

    @Column(nullable = false)
    private String actor;

    private String ipAddress;

    @Column(nullable = false)
    private Instant timestamp;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        timestamp = Instant.now();
    }
}
