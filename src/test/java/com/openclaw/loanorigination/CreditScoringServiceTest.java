package com.openclaw.loanorigination;

import com.openclaw.loanorigination.dto.CreditAssessmentDTO;
import com.openclaw.loanorigination.entity.LoanApplication;
import com.openclaw.loanorigination.service.CreditScoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class CreditScoringServiceTest {

    @Autowired
    private CreditScoringService creditScoringService;

    @Test
    void makeDecision_withHighScore_shouldApprove() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(750);
        assertEquals(LoanApplication.Decision.APPROVED, decision);
    }

    @Test
    void makeDecision_withMediumScore_shouldRequireManualReview() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(650);
        assertEquals(LoanApplication.Decision.MANUAL_REVIEW, decision);
    }

    @Test
    void makeDecision_withLowScore_shouldDecline() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(550);
        assertEquals(LoanApplication.Decision.DECLINED, decision);
    }

    @Test
    void makeDecision_atApproveThreshold_shouldApprove() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(700);
        assertEquals(LoanApplication.Decision.APPROVED, decision);
    }

    @Test
    void makeDecision_atManualReviewThreshold_shouldRequireManualReview() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(600);
        assertEquals(LoanApplication.Decision.MANUAL_REVIEW, decision);
    }

    @Test
    void makeDecision_justBelowApproveThreshold_shouldRequireManualReview() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(699);
        assertEquals(LoanApplication.Decision.MANUAL_REVIEW, decision);
    }

    @Test
    void makeDecision_justBelowManualReviewThreshold_shouldDecline() {
        LoanApplication.Decision decision = creditScoringService.makeDecision(599);
        assertEquals(LoanApplication.Decision.DECLINED, decision);
    }
}
