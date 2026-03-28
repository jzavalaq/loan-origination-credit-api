package com.fintech.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing a credit assessment for a loan application.
 *
 * <p>Stores the calculated credit score, individual factor scores,
 * debt-to-income ratio, and risk level determination.</p>
 */
@Entity
@Table(name = "credit_assessments", indexes = {
    @Index(name = "idx_credit_assessment_application_id", columnList = "applicationId"),
    @Index(name = "idx_credit_assessment_risk_level", columnList = "riskLevel"),
    @Index(name = "idx_credit_assessment_credit_score", columnList = "creditScore")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Integer creditScore;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal debtToIncomeRatio;

    @Column(nullable = false)
    private Integer incomeScore; // 0-100

    @Column(nullable = false)
    private Integer employmentScore; // 0-100

    @Column(nullable = false)
    private Integer creditHistoryScore; // 0-100

    @Column(nullable = false)
    private Integer debtScore; // 0-100

    @Column(nullable = false, length = 1000)
    private String scoreFactors; // JSON or delimited factors

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(nullable = false, updatable = false)
    private Instant assessedAt;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }

    @PrePersist
    protected void onCreate() {
        assessedAt = Instant.now();
    }
}
