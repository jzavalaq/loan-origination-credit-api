package com.fintech.loan.repository;

import com.fintech.loan.entity.CreditAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CreditAssessment entities.
 *
 * <p>Provides data access operations for credit assessments including
 * custom queries for filtering by application ID, risk level, and credit score range.</p>
 *
 * @see CreditAssessment
 */
@Repository
public interface CreditAssessmentRepository extends JpaRepository<CreditAssessment, Long> {

    Optional<CreditAssessment> findByApplicationId(Long applicationId);

    List<CreditAssessment> findByRiskLevel(CreditAssessment.RiskLevel riskLevel);

    @Query("SELECT AVG(ca.creditScore) FROM CreditAssessment ca")
    Double findAverageCreditScore();

    @Query("SELECT ca FROM CreditAssessment ca WHERE ca.creditScore BETWEEN :min AND :max")
    List<CreditAssessment> findByCreditScoreBetween(@Param("min") Integer min, @Param("max") Integer max);
}
