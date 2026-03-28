package com.fintech.loan.service;

import com.fintech.loan.dto.CreditAssessmentDTO;
import com.fintech.loan.entity.Applicant;
import com.fintech.loan.entity.CreditAssessment;
import com.fintech.loan.entity.LoanApplication;
import com.fintech.loan.exception.BusinessException;
import com.fintech.loan.repository.CreditAssessmentRepository;
import com.fintech.loan.repository.LoanApplicationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditScoringServiceTest {

    @Mock
    private CreditAssessmentRepository creditAssessmentRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CreditScoringService creditScoringService;

    private LoanApplication testApplication;
    private Applicant testApplicant;
    private CreditAssessment testAssessment;

    @BeforeEach
    void setUp() {
        testApplicant = Applicant.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .ssn("123-45-6789")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .address("123 Main Street")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(new BigDecimal("75000.00"))
                .employerName("Acme Corp")
                .employmentYears(5)
                .creditHistoryYears(7)
                .totalDebt(new BigDecimal("15000.00"))
                .build();

        testApplication = LoanApplication.builder()
                .id(1L)
                .applicant(testApplicant)
                .requestedAmount(new BigDecimal("50000.00"))
                .loanTermMonths(36)
                .loanPurpose("Home improvement")
                .status(LoanApplication.ApplicationStatus.SUBMITTED)
                .build();

        testAssessment = CreditAssessment.builder()
                .id(1L)
                .applicationId(1L)
                .creditScore(720)
                .debtToIncomeRatio(new BigDecimal("0.2000"))
                .incomeScore(70)
                .employmentScore(80)
                .creditHistoryScore(70)
                .debtScore(70)
                .scoreFactors("[]")
                .riskLevel(CreditAssessment.RiskLevel.LOW)
                .build();
    }

    @Test
    @DisplayName("makeDecision - high credit score - returns APPROVED")
    void makeDecision_highCreditScore_returnsApproved() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(750);
        assertEquals(LoanApplication.Decision.APPROVED, decision);
    }

    @Test
    @DisplayName("makeDecision - medium credit score - returns MANUAL_REVIEW")
    void makeDecision_mediumCreditScore_returnsManualReview() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(650);
        assertEquals(LoanApplication.Decision.MANUAL_REVIEW, decision);
    }

    @Test
    @DisplayName("makeDecision - low credit score - returns DECLINED")
    void makeDecision_lowCreditScore_returnsDeclined() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(500);
        assertEquals(LoanApplication.Decision.DECLINED, decision);
    }

    @Test
    @DisplayName("getDecisionReason - APPROVED decision - returns correct reason")
    void getDecisionReason_approvedDecision_returnsCorrectReason() {
        String reason = creditScoringService.getDecisionReason(LoanApplication.Decision.APPROVED, 750);
        assertTrue(reason.contains("750"));
        assertTrue(reason.contains("approval threshold"));
    }

    @Test
    @DisplayName("getDecisionReason - MANUAL_REVIEW decision - returns correct reason")
    void getDecisionReason_manualReviewDecision_returnsCorrectReason() {
        String reason = creditScoringService.getDecisionReason(LoanApplication.Decision.MANUAL_REVIEW, 650);
        assertTrue(reason.contains("650"));
        assertTrue(reason.contains("manual review"));
    }

    @Test
    @DisplayName("getDecisionReason - DECLINED decision - returns correct reason")
    void getDecisionReason_declinedDecision_returnsCorrectReason() {
        String reason = creditScoringService.getDecisionReason(LoanApplication.Decision.DECLINED, 500);
        assertTrue(reason.contains("500"));
        assertTrue(reason.contains("below minimum threshold"));
    }

    @Test
    @DisplayName("assessApplication - valid application - returns assessment")
    void assessApplication_validApplication_returnsAssessment() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(creditAssessmentRepository.save(any(CreditAssessment.class))).thenReturn(testAssessment);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(testApplication);

        CreditAssessmentDTO.Response response = creditScoringService.assessApplication(1L);

        assertNotNull(response);
        assertEquals(1L, response.getApplicationId());
        assertTrue(response.getCreditScore() >= 300);
        assertTrue(response.getCreditScore() <= 850);
        verify(creditAssessmentRepository).save(any(CreditAssessment.class));
        verify(auditLogService).log(anyString(), anyLong(), anyString(), any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("assessApplication - application not found - throws BusinessException")
    void assessApplication_applicationNotFound_throwsBusinessException() {
        when(loanApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> creditScoringService.assessApplication(999L));
    }

    @Test
    @DisplayName("assessApplication - already assessed - returns existing assessment")
    void assessApplication_alreadyAssessed_returnsExistingAssessment() {
        testApplication.setCreditAssessment(testAssessment);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        CreditAssessmentDTO.Response response = creditScoringService.assessApplication(1L);

        assertNotNull(response);
        verify(creditAssessmentRepository, never()).save(any());
    }
}
