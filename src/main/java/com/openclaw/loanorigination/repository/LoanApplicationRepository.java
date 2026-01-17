package com.openclaw.loanorigination.repository;

import com.openclaw.loanorigination.entity.LoanApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LoanApplication entities.
 *
 * <p>Provides data access operations for loan applications including
 * custom queries for filtering by status, decision, applicant, and date range.</p>
 *
 * @see LoanApplication
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    @Query("SELECT la FROM LoanApplication la JOIN FETCH la.applicant LEFT JOIN FETCH la.creditAssessment WHERE la.id = :id")
    Optional<LoanApplication> findByIdWithApplicantAndAssessment(@Param("id") Long id);

    Page<LoanApplication> findByApplicantId(Long applicantId, Pageable pageable);

    Page<LoanApplication> findByStatus(LoanApplication.ApplicationStatus status, Pageable pageable);

    Page<LoanApplication> findByDecision(LoanApplication.Decision decision, Pageable pageable);

    @Query("SELECT la FROM LoanApplication la WHERE la.createdAt BETWEEN :start AND :end")
    Page<LoanApplication> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Query("SELECT la FROM LoanApplication la JOIN la.applicant a WHERE " +
           "(:status IS NULL OR la.status = :status) AND " +
           "(:decision IS NULL OR la.decision = :decision) AND " +
           "(:applicantId IS NULL OR a.id = :applicantId)")
    Page<LoanApplication> findByFilters(
            @Param("status") LoanApplication.ApplicationStatus status,
            @Param("decision") LoanApplication.Decision decision,
            @Param("applicantId") Long applicantId,
            Pageable pageable);

    long countByStatus(LoanApplication.ApplicationStatus status);

    long countByDecision(LoanApplication.Decision decision);
}
