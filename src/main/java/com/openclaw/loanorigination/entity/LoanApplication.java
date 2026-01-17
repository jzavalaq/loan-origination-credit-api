package com.openclaw.loanorigination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing a loan application.
 *
 * <p>Tracks the complete lifecycle of a loan application from draft
 * through decision and disbursement, including credit assessment results.</p>
 */
@Entity
@Table(name = "loan_applications", indexes = {
    @Index(name = "idx_loan_applicant_id", columnList = "applicant_id"),
    @Index(name = "idx_loan_status", columnList = "status"),
    @Index(name = "idx_loan_decision", columnList = "decision"),
    @Index(name = "idx_loan_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private Integer loanTermMonths;

    @Column(nullable = false)
    private String loanPurpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    private String decisionReason;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "assessment_id")
    private CreditAssessment creditAssessment;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    private Instant submittedAt;

    private Instant decidedAt;

    public enum ApplicationStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        DECIDED,
        DISBURSED,
        CANCELLED
    }

    public enum Decision {
        APPROVED,
        MANUAL_REVIEW,
        DECLINED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = ApplicationStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
