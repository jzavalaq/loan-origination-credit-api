package com.openclaw.loanorigination.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Objects for LoanApplication operations.
 *
 * <p>Contains request and response DTOs for creating, updating, and retrieving loan applications.</p>
 */
public class LoanApplicationDTO {

    /**
     * Request DTO for creating a new loan application.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Applicant ID is required")
        @Positive(message = "Applicant ID must be positive")
        private Long applicantId;

        @NotNull(message = "Requested amount is required")
        @Positive(message = "Requested amount must be positive")
        @DecimalMin(value = "1000.00", message = "Minimum loan amount is $1,000")
        @DecimalMax(value = "1000000.00", message = "Maximum loan amount is $1,000,000")
        private BigDecimal requestedAmount;

        @NotNull(message = "Loan term is required")
        @Positive(message = "Loan term must be positive")
        @Min(value = 6, message = "Minimum loan term is 6 months")
        @Max(value = 360, message = "Maximum loan term is 360 months")
        private Integer loanTermMonths;

        @NotBlank(message = "Loan purpose is required")
        @Size(max = 200)
        private String loanPurpose;
    }

    /**
     * Request DTO for updating loan application status.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        @NotBlank(message = "Status is required")
        private String status;
    }

    /**
     * Request DTO for overriding a loan application decision.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DecisionRequest {
        @NotBlank(message = "Decision is required")
        private String decision;

        @Size(max = 500)
        private String decisionReason;
    }

    /**
     * Response DTO for loan application data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long applicantId;
        private String applicantName;
        private BigDecimal requestedAmount;
        private Integer loanTermMonths;
        private String loanPurpose;
        private String status;
        private String decision;
        private String decisionReason;
        private CreditAssessmentDTO.Response creditAssessment;
        private Long version;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant submittedAt;
        private Instant decidedAt;
    }

    /**
     * Paginated list response DTO for loan applications.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private java.util.List<Response> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
