package com.openclaw.loanorigination.repository;

import com.openclaw.loanorigination.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

/**
 * Repository for AuditLog entities.
 *
 * <p>Provides data access operations for audit logs including
 * custom queries for filtering by entity type, action, actor, and timestamp.</p>
 *
 * @see AuditLog
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findByActor(String actor, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :start AND :end ORDER BY al.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(@Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:entityType IS NULL OR al.entityType = :entityType) AND " +
           "(:entityId IS NULL OR al.entityId = :entityId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:actor IS NULL OR al.actor = :actor) AND " +
           "(:start IS NULL OR al.timestamp >= :start) AND " +
           "(:end IS NULL OR al.timestamp <= :end)")
    Page<AuditLog> findByFilters(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("action") String action,
            @Param("actor") String actor,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    List<AuditLog> findByEntityIdOrderByTimestampDesc(Long entityId);
}
