package com.fintech.loan.service;

import com.fintech.loan.dto.CreditAssessmentDTO;
import com.fintech.loan.entity.Applicant;
import com.fintech.loan.entity.CreditAssessment;
import com.fintech.loan.entity.LoanApplication;
import com.fintech.loan.exception.BusinessException;
import com.fintech.loan.repository.CreditAssessmentRepository;
import com.fintech.loan.repository.LoanApplicationRepository;
import com.fintech.loan.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for credit scoring and risk assessment.
 *
 * <p>Provides automated credit scoring based on multiple factors including income,
 * employment history, credit history, and debt-to-income ratio. Uses weighted
 * scoring to calculate a FICO-style credit score (300-850).</p>
 *
 * @see CreditAssessment
 * @see CreditAssessmentRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditScoringService {

    private final CreditAssessmentRepository creditAssessmentRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * Performs credit assessment on a loan application.
     *
     * @param applicationId the loan application ID
     * @return the credit assessment response
     * @throws BusinessException if application is not found or has no applicant
     */
    @Transactional
    public CreditAssessmentDTO.Response assessApplication(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found: " + applicationId));

        if (application.getCreditAssessment() != null) {
            log.info("Application {} already assessed", applicationId);
            return toResponse(application.getCreditAssessment());
        }

        Applicant applicant = application.getApplicant();
        if (applicant == null) {
            throw new BusinessException("Application has no applicant");
        }

        // Calculate individual scores
        int incomeScore = calculateIncomeScore(applicant.getAnnualIncome());
        int employmentScore = calculateEmploymentScore(applicant.getEmploymentYears());
        int creditHistoryScore = calculateCreditHistoryScore(applicant.getCreditHistoryYears());
        int debtScore = calculateDebtScore(applicant.getTotalDebt(), applicant.getAnnualIncome());

        // Calculate weighted credit score
        int creditScore = calculateWeightedScore(incomeScore, employmentScore, creditHistoryScore, debtScore);

        // Calculate debt-to-income ratio
        BigDecimal debtToIncomeRatio = calculateDebtToIncomeRatio(applicant.getTotalDebt(), applicant.getAnnualIncome());

        // Determine risk level
        CreditAssessment.RiskLevel riskLevel = determineRiskLevel(creditScore);

        // Build score factors
        List<String> factors = buildScoreFactors(incomeScore, employmentScore, creditHistoryScore, debtScore);
        String scoreFactorsJson = serializeFactors(factors);

        CreditAssessment assessment = CreditAssessment.builder()
                .applicationId(applicationId)
                .creditScore(creditScore)
                .debtToIncomeRatio(debtToIncomeRatio)
                .incomeScore(incomeScore)
                .employmentScore(employmentScore)
                .creditHistoryScore(creditHistoryScore)
                .debtScore(debtScore)
                .scoreFactors(scoreFactorsJson)
                .riskLevel(riskLevel)
                .build();

        CreditAssessment saved = creditAssessmentRepository.save(assessment);

        // Link assessment to application
        application.setCreditAssessment(saved);
        loanApplicationRepository.save(application);

        auditLogService.log(
                "CREDIT_ASSESSMENT", saved.getId(), "CREATE",
                null, saved, "system", null
        );

        log.info("Credit assessment completed for application {}: score={}", applicationId, creditScore);
        return toResponse(saved);
    }

    /**
     * Retrieves the credit assessment for a loan application.
     *
     * @param applicationId the loan application ID
     * @return the credit assessment response
     * @throws BusinessException if assessment is not found
     */
    @Transactional(readOnly = true)
    public CreditAssessmentDTO.Response getAssessment(Long applicationId) {
        CreditAssessment assessment = creditAssessmentRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new BusinessException("Assessment not found for application: " + applicationId));
        return toResponse(assessment);
    }

    /**
     * Makes a decision based on the credit score.
     *
     * @param creditScore the credit score (300-850)
     * @return the decision (APPROVED, MANUAL_REVIEW, or DECLINED)
     */
    public LoanApplication.Decision makeDecision(int creditScore) {
        if (creditScore >= Constants.APPROVE_THRESHOLD) {
            return LoanApplication.Decision.APPROVED;
        } else if (creditScore >= Constants.MANUAL_REVIEW_THRESHOLD) {
            return LoanApplication.Decision.MANUAL_REVIEW;
        } else {
            return LoanApplication.Decision.DECLINED;
        }
    }

    /**
     * Gets the human-readable reason for a decision.
     *
     * @param decision the decision
     * @param creditScore the credit score
     * @return the decision reason
     */
    public String getDecisionReason(LoanApplication.Decision decision, int creditScore) {
        return switch (decision) {
            case APPROVED -> "Credit score " + creditScore + " meets approval threshold (>=700)";
            case MANUAL_REVIEW -> "Credit score " + creditScore + " requires manual review (600-699)";
            case DECLINED -> "Credit score " + creditScore + " below minimum threshold (<600)";
        };
    }

    private int calculateIncomeScore(BigDecimal annualIncome) {
        double income = annualIncome.doubleValue();
        if (income >= 150000) return 100;
        if (income >= 100000) return 85;
        if (income >= 75000) return 70;
        if (income >= 50000) return 55;
        if (income >= 35000) return 40;
        return 25;
    }

    private int calculateEmploymentScore(Integer employmentYears) {
        if (employmentYears >= 10) return 100;
        if (employmentYears >= 5) return 80;
        if (employmentYears >= 3) return 60;
        if (employmentYears >= 1) return 40;
        return 20;
    }

    private int calculateCreditHistoryScore(Integer creditHistoryYears) {
        if (creditHistoryYears >= 15) return 100;
        if (creditHistoryYears >= 10) return 85;
        if (creditHistoryYears >= 7) return 70;
        if (creditHistoryYears >= 5) return 55;
        if (creditHistoryYears >= 2) return 40;
        return 25;
    }

    private int calculateDebtScore(BigDecimal totalDebt, BigDecimal annualIncome) {
        if (annualIncome.compareTo(BigDecimal.ZERO) == 0) return 20;

        BigDecimal debtRatio = totalDebt.divide(annualIncome, 4, RoundingMode.HALF_UP);
        double ratio = debtRatio.doubleValue();

        if (ratio <= 0.10) return 100;
        if (ratio <= 0.20) return 85;
        if (ratio <= 0.30) return 70;
        if (ratio <= 0.40) return 55;
        if (ratio <= 0.50) return 40;
        return 20;
    }

    private int calculateWeightedScore(int incomeScore, int employmentScore, int creditHistoryScore, int debtScore) {
        double weightedScore = (incomeScore * Constants.INCOME_WEIGHT) +
                (employmentScore * Constants.EMPLOYMENT_WEIGHT) +
                (creditHistoryScore * Constants.CREDIT_HISTORY_WEIGHT) +
                (debtScore * Constants.DEBT_WEIGHT);

        // Scale to credit score range (300-850)
        int score = Constants.CREDIT_SCORE_BASE + (int) ((weightedScore / 100.0) * (Constants.CREDIT_SCORE_MAX - Constants.CREDIT_SCORE_BASE));
        return Math.max(Constants.CREDIT_SCORE_MIN, Math.min(Constants.CREDIT_SCORE_MAX, score));
    }

    private BigDecimal calculateDebtToIncomeRatio(BigDecimal totalDebt, BigDecimal annualIncome) {
        if (annualIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return totalDebt.divide(annualIncome, 4, RoundingMode.HALF_UP);
    }

    private CreditAssessment.RiskLevel determineRiskLevel(int creditScore) {
        if (creditScore >= Constants.APPROVE_THRESHOLD) return CreditAssessment.RiskLevel.LOW;
        if (creditScore >= Constants.MANUAL_REVIEW_THRESHOLD) return CreditAssessment.RiskLevel.MEDIUM;
        return CreditAssessment.RiskLevel.HIGH;
    }

    private List<String> buildScoreFactors(int incomeScore, int employmentScore, int creditHistoryScore, int debtScore) {
        List<String> factors = new ArrayList<>();

        if (incomeScore < 50) factors.add("Low income");
        if (employmentScore < 50) factors.add("Short employment history");
        if (creditHistoryScore < 50) factors.add("Limited credit history");
        if (debtScore < 50) factors.add("High debt-to-income ratio");

        if (factors.isEmpty()) {
            factors.add("No significant risk factors identified");
        }

        return factors;
    }

    private String serializeFactors(List<String> factors) {
        try {
            return objectMapper.writeValueAsString(factors);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private CreditAssessmentDTO.Response toResponse(CreditAssessment assessment) {
        return CreditAssessmentDTO.Response.builder()
                .id(assessment.getId())
                .applicationId(assessment.getApplicationId())
                .creditScore(assessment.getCreditScore())
                .debtToIncomeRatio(assessment.getDebtToIncomeRatio())
                .incomeScore(assessment.getIncomeScore())
                .employmentScore(assessment.getEmploymentScore())
                .creditHistoryScore(assessment.getCreditHistoryScore())
                .debtScore(assessment.getDebtScore())
                .scoreFactors(assessment.getScoreFactors())
                .riskLevel(assessment.getRiskLevel().name())
                .assessedAt(assessment.getAssessedAt())
                .build();
    }
}
