package com.openclaw.loanorigination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Objects for CreditAssessment operations.
 *
 * <p>Contains response DTOs for credit assessment data.</p>
 */
public class CreditAssessmentDTO {

    /**
     * Response DTO for credit assessment data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long applicationId;
        private Integer creditScore;
        private BigDecimal debtToIncomeRatio;
        private Integer incomeScore;
        private Integer employmentScore;
        private Integer creditHistoryScore;
        private Integer debtScore;
        private String scoreFactors;
        private String riskLevel;
        private Instant assessedAt;
    }
}
