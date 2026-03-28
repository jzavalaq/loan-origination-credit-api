package com.fintech.loan.service;

import com.fintech.loan.dto.LoanApplicationDTO;
import com.fintech.loan.entity.Applicant;
import com.fintech.loan.entity.CreditAssessment;
import com.fintech.loan.entity.LoanApplication;
import com.fintech.loan.exception.BusinessException;
import com.fintech.loan.exception.ResourceNotFoundException;
import com.fintech.loan.repository.LoanApplicationRepository;
import com.fintech.loan.util.Constants;
import com.fintech.loan.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing loan applications.
 *
 * <p>Handles the complete lifecycle of loan applications including creation,
 * submission, credit assessment, decision management, and deletion.</p>
 *
 * @see LoanApplication
 * @see LoanApplicationRepository
 * @see CreditScoringService
 * @see AuditLogService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicantService applicantService;
    private final CreditScoringService creditScoringService;
    private final AuditLogService auditLogService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Creates a new loan application for an applicant.
     *
     * @param dto the loan application creation request
     * @return the created loan application response
     * @throws ResourceNotFoundException if the applicant is not found
     */
    @Transactional
    public LoanApplicationDTO.Response createApplication(LoanApplicationDTO.CreateRequest dto) {
        Applicant applicant = applicantService.getEntityById(dto.getApplicantId());

        LoanApplication application = LoanApplication.builder()
                .applicant(applicant)
                .requestedAmount(dto.getRequestedAmount())
                .loanTermMonths(dto.getLoanTermMonths())
                .loanPurpose(dto.getLoanPurpose())
                .status(LoanApplication.ApplicationStatus.DRAFT)
                .build();

        LoanApplication saved = loanApplicationRepository.save(application);

        auditLogService.log(
                "LOAN_APPLICATION", saved.getId(), "CREATE",
                null, saved, getCurrentActor(), getClientIp()
        );

        log.info("Created loan application {} for applicant {}", saved.getId(), applicant.getId());
        return toResponse(saved);
    }

    /**
     * Retrieves a loan application by ID.
     *
     * @param id the application ID
     * @return the loan application response
     * @throws ResourceNotFoundException if application is not found
     */
    @Transactional(readOnly = true)
    public LoanApplicationDTO.Response getApplication(Long id) {
        LoanApplication application = loanApplicationRepository.findByIdWithApplicantAndAssessment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
        return toResponse(application);
    }

    /**
     * Lists loan applications with optional filtering and pagination.
     *
     * @param applicantId optional filter by applicant ID
     * @param status optional filter by application status
     * @param page the page number (0-indexed)
     * @param size the page size (max 100)
     * @return paginated list of loan applications
     */
    @Transactional(readOnly = true)
    public LoanApplicationDTO.ListResponse listApplications(
            Long applicantId, String status, int page, int size) {

        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        if (page < Constants.MIN_PAGE_NUMBER) page = Constants.MIN_PAGE_NUMBER;
        if (safeSize < 1) safeSize = Constants.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<LoanApplication> applications;
        if (applicantId != null) {
            applications = loanApplicationRepository.findByApplicantId(applicantId, pageable);
        } else if (status != null && !status.isEmpty()) {
            try {
                LoanApplication.ApplicationStatus appStatus = LoanApplication.ApplicationStatus.valueOf(status.toUpperCase());
                applications = loanApplicationRepository.findByStatus(appStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid status: " + status);
            }
        } else {
            applications = loanApplicationRepository.findAll(pageable);
        }

        List<LoanApplicationDTO.Response> content = applications.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return LoanApplicationDTO.ListResponse.builder()
                .content(content)
                .pageNumber(applications.getNumber())
                .pageSize(applications.getSize())
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .last(applications.isLast())
                .build();
    }

    /**
     * Submits a draft loan application for processing.
     *
     * @param id the application ID
     * @return the updated application with SUBMITTED status
     * @throws ResourceNotFoundException if application is not found
     * @throws BusinessException if application is not in DRAFT status
     */
    @Transactional
    public LoanApplicationDTO.Response submitApplication(Long id) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        if (application.getStatus() != LoanApplication.ApplicationStatus.DRAFT) {
            throw new BusinessException("Only DRAFT applications can be submitted");
        }

        LoanApplication oldApplication = copyApplication(application);

        application.setStatus(LoanApplication.ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(Instant.now());

        LoanApplication saved = loanApplicationRepository.save(application);

        auditLogService.log(
                "LOAN_APPLICATION", saved.getId(), "SUBMIT",
                oldApplication, saved, getCurrentActor(), getClientIp()
        );

        log.info("Submitted loan application {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Performs credit assessment on a submitted application.
     *
     * @param id the application ID
     * @return the application with credit score and decision
     * @throws ResourceNotFoundException if application is not found
     * @throws BusinessException if application is not in SUBMITTED status
     */
    @Transactional
    public LoanApplicationDTO.Response assessApplication(Long id) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        if (application.getStatus() != LoanApplication.ApplicationStatus.SUBMITTED) {
            throw new BusinessException("Only SUBMITTED applications can be assessed");
        }

        application.setStatus(LoanApplication.ApplicationStatus.UNDER_REVIEW);
        loanApplicationRepository.save(application);

        // Perform credit assessment
        creditScoringService.assessApplication(id);

        // Refresh to get the assessment
        LoanApplication updated = loanApplicationRepository.findById(id).orElseThrow();

        // Get the credit score and make a decision
        CreditAssessment assessment = updated.getCreditAssessment();
        if (assessment == null) {
            throw new BusinessException("Credit assessment failed");
        }

        LoanApplication.Decision decision = creditScoringService.makeDecision(assessment.getCreditScore());
        String decisionReason = creditScoringService.getDecisionReason(decision, assessment.getCreditScore());

        updated.setDecision(decision);
        updated.setDecisionReason(decisionReason);
        updated.setStatus(LoanApplication.ApplicationStatus.DECIDED);
        updated.setDecidedAt(Instant.now());

        LoanApplication saved = loanApplicationRepository.save(updated);

        auditLogService.log(
                "LOAN_APPLICATION", saved.getId(), "ASSESS_AND_DECIDE",
                null, saved, getCurrentActor(), getClientIp()
        );

        log.info("Assessed loan application {}: score={}, decision={}",
                saved.getId(), assessment.getCreditScore(), decision);
        return toResponse(saved);
    }

    /**
     * Updates the status of a loan application.
     *
     * @param id the application ID
     * @param dto the status update request
     * @return the updated application
     * @throws ResourceNotFoundException if application is not found
     * @throws BusinessException if status value is invalid
     */
    @Transactional
    public LoanApplicationDTO.Response updateStatus(Long id, LoanApplicationDTO.StatusUpdateRequest dto) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        LoanApplication.ApplicationStatus newStatus;
        try {
            newStatus = LoanApplication.ApplicationStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + dto.getStatus());
        }

        LoanApplication oldApplication = copyApplication(application);

        application.setStatus(newStatus);
        LoanApplication saved = loanApplicationRepository.save(application);

        auditLogService.log(
                "LOAN_APPLICATION", saved.getId(), "UPDATE_STATUS",
                oldApplication, saved, getCurrentActor(), getClientIp()
        );

        log.info("Updated status for loan application {} to {}", saved.getId(), newStatus);
        return toResponse(saved);
    }

    /**
     * Overrides the decision on a loan application.
     *
     * @param id the application ID
     * @param dto the decision override request
     * @return the updated application with new decision
     * @throws ResourceNotFoundException if application is not found
     * @throws BusinessException if application is not in an overridable status
     */
    @Transactional
    public LoanApplicationDTO.Response overrideDecision(Long id, LoanApplicationDTO.DecisionRequest dto) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        if (application.getStatus() != LoanApplication.ApplicationStatus.DECIDED &&
            application.getStatus() != LoanApplication.ApplicationStatus.UNDER_REVIEW) {
            throw new BusinessException("Cannot override decision for application in " + application.getStatus() + " status");
        }

        LoanApplication oldApplication = copyApplication(application);

        LoanApplication.Decision newDecision;
        try {
            newDecision = LoanApplication.Decision.valueOf(dto.getDecision().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid decision: " + dto.getDecision());
        }

        application.setDecision(newDecision);
        application.setDecisionReason(dto.getDecisionReason() != null ? dto.getDecisionReason() : "Manual override");
        application.setStatus(LoanApplication.ApplicationStatus.DECIDED);
        application.setDecidedAt(Instant.now());

        LoanApplication saved = loanApplicationRepository.save(application);

        auditLogService.log(
                "LOAN_APPLICATION", saved.getId(), "OVERRIDE_DECISION",
                oldApplication, saved, getCurrentActor(), getClientIp()
        );

        log.info("Overrode decision for loan application {} to {}", saved.getId(), newDecision);
        return toResponse(saved);
    }

    /**
     * Deletes a loan application.
     *
     * @param id the application ID
     * @throws ResourceNotFoundException if application is not found
     * @throws BusinessException if application is in DISBURSED status
     */
    @Transactional
    public void deleteApplication(Long id) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        if (application.getStatus() == LoanApplication.ApplicationStatus.DISBURSED) {
            throw new BusinessException("Cannot delete a disbursed loan");
        }

        auditLogService.log(
                "LOAN_APPLICATION", application.getId(), "DELETE",
                application, null, getCurrentActor(), getClientIp()
        );

        loanApplicationRepository.delete(application);
        log.info("Deleted loan application {}", id);
    }

    /**
     * Converts a LoanApplication entity to a response DTO.
     *
     * @param application the loan application entity
     * @return the response DTO
     */
    private LoanApplicationDTO.Response toResponse(LoanApplication application) {
        Applicant applicant = application.getApplicant();
        String applicantName = applicant != null ?
                applicant.getFirstName() + " " + applicant.getLastName() : null;

        CreditAssessment assessment = application.getCreditAssessment();
        var assessmentResponse = assessment != null ?
                com.fintech.loan.dto.CreditAssessmentDTO.Response.builder()
                        .id(assessment.getId())
                        .applicationId(assessment.getApplicationId())
                        .creditScore(assessment.getCreditScore())
                        .debtToIncomeRatio(assessment.getDebtToIncomeRatio())
                        .incomeScore(assessment.getIncomeScore())
                        .employmentScore(assessment.getEmploymentScore())
                        .creditHistoryScore(assessment.getCreditHistoryScore())
                        .debtScore(assessment.getDebtScore())
                        .scoreFactors(assessment.getScoreFactors())
                        .riskLevel(assessment.getRiskLevel() != null ? assessment.getRiskLevel().name() : null)
                        .assessedAt(assessment.getAssessedAt())
                        .build() : null;

        return LoanApplicationDTO.Response.builder()
                .id(application.getId())
                .applicantId(applicant != null ? applicant.getId() : null)
                .applicantName(applicantName)
                .requestedAmount(application.getRequestedAmount())
                .loanTermMonths(application.getLoanTermMonths())
                .loanPurpose(application.getLoanPurpose())
                .status(application.getStatus() != null ? application.getStatus().name() : null)
                .decision(application.getDecision() != null ? application.getDecision().name() : null)
                .decisionReason(application.getDecisionReason())
                .creditAssessment(assessmentResponse)
                .version(application.getVersion())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .submittedAt(application.getSubmittedAt())
                .decidedAt(application.getDecidedAt())
                .build();
    }

    /**
     * Creates a copy of a LoanApplication entity for audit logging.
     *
     * @param source the source application
     * @return a copy of the application with key fields
     */
    private LoanApplication copyApplication(LoanApplication source) {
        return LoanApplication.builder()
                .id(source.getId())
                .status(source.getStatus())
                .decision(source.getDecision())
                .decisionReason(source.getDecisionReason())
                .build();
    }

    /**
     * Gets the current actor from the request context.
     *
     * @return the current actor identifier
     */
    private String getCurrentActor() {
        return requestContextUtil.getCurrentActor();
    }

    /**
     * Gets the client IP address from the request context.
     *
     * @return the client IP address
     */
    private String getClientIp() {
        return requestContextUtil.getClientIp();
    }
}
