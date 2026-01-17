package com.openclaw.loanorigination.controller;

import com.openclaw.loanorigination.dto.LoanApplicationDTO;
import com.openclaw.loanorigination.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing loan applications.
 *
 * <p>Handles the complete lifecycle of loan applications including creation,
 * submission, credit assessment, and decision management.</p>
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Applications", description = "Operations for managing loan applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    /**
     * Creates a new loan application for an applicant.
     *
     * @param request the loan application creation request
     * @return the created loan application
     */
    @Operation(summary = "Create a new loan application", description = "Creates a draft loan application for the specified applicant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Application created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Applicant not found")
    })
    @PostMapping
    public ResponseEntity<LoanApplicationDTO.Response> createApplication(
            @Valid @RequestBody LoanApplicationDTO.CreateRequest request) {
        log.info("Creating loan application for applicant ID: {}", request.getApplicantId());
        LoanApplicationDTO.Response response = loanApplicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a loan application by ID.
     *
     * @param id the application ID
     * @return the loan application details
     */
    @Operation(summary = "Get loan application by ID", description = "Retrieves a specific loan application with credit assessment details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application found"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationDTO.Response> getApplication(
            @Parameter(description = "Application ID") @PathVariable Long id) {
        log.debug("Fetching loan application with ID: {}", id);
        LoanApplicationDTO.Response response = loanApplicationService.getApplication(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists loan applications with optional filtering.
     *
     * @param applicantId optional filter by applicant ID
     * @param status optional filter by application status
     * @param page the page number
     * @param size the page size
     * @return paginated list of loan applications
     */
    @Operation(summary = "List loan applications", description = "Retrieves a paginated list of loan applications with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<LoanApplicationDTO.ListResponse> listApplications(
            @Parameter(description = "Filter by applicant ID") @RequestParam(required = false) Long applicantId,
            @Parameter(description = "Filter by status (DRAFT, SUBMITTED, UNDER_REVIEW, DECIDED, DISBURSED, CANCELLED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("Listing applications: applicantId={}, status={}, page={}, size={}", applicantId, status, page, size);
        LoanApplicationDTO.ListResponse response = loanApplicationService.listApplications(applicantId, status, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Submits a draft loan application for processing.
     *
     * @param id the application ID
     * @return the updated application with SUBMITTED status
     */
    @Operation(summary = "Submit a loan application", description = "Changes application status from DRAFT to SUBMITTED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Application not in DRAFT status"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplicationDTO.Response> submitApplication(
            @Parameter(description = "Application ID") @PathVariable Long id) {
        log.info("Submitting loan application with ID: {}", id);
        LoanApplicationDTO.Response response = loanApplicationService.submitApplication(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Performs credit assessment on a submitted application.
     *
     * @param id the application ID
     * @return the application with credit score and decision
     */
    @Operation(summary = "Assess a loan application", description = "Performs credit scoring and makes a decision on the application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessment completed successfully"),
        @ApiResponse(responseCode = "400", description = "Application not in SUBMITTED status"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping("/{id}/assess")
    public ResponseEntity<LoanApplicationDTO.Response> assessApplication(
            @Parameter(description = "Application ID") @PathVariable Long id) {
        log.info("Assessing loan application with ID: {}", id);
        LoanApplicationDTO.Response response = loanApplicationService.assessApplication(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the status of a loan application.
     *
     * @param id the application ID
     * @param request the status update request
     * @return the updated application
     */
    @Operation(summary = "Update application status", description = "Manually updates the status of a loan application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<LoanApplicationDTO.Response> updateStatus(
            @Parameter(description = "Application ID") @PathVariable Long id,
            @Valid @RequestBody LoanApplicationDTO.StatusUpdateRequest request) {
        log.info("Updating status for application ID {}: {}", id, request.getStatus());
        LoanApplicationDTO.Response response = loanApplicationService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Overrides the decision on a loan application.
     *
     * @param id the application ID
     * @param request the decision override request
     * @return the updated application with new decision
     */
    @Operation(summary = "Override application decision", description = "Manually overrides the automated decision on a loan application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Decision overridden successfully"),
        @ApiResponse(responseCode = "400", description = "Application not in overridable status"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping("/{id}/override")
    public ResponseEntity<LoanApplicationDTO.Response> overrideDecision(
            @Parameter(description = "Application ID") @PathVariable Long id,
            @Valid @RequestBody LoanApplicationDTO.DecisionRequest request) {
        log.info("Overriding decision for application ID {}: {}", id, request.getDecision());
        LoanApplicationDTO.Response response = loanApplicationService.overrideDecision(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a loan application.
     *
     * @param id the application ID
     * @return no content on successful deletion
     */
    @Operation(summary = "Delete a loan application", description = "Removes a loan application from the system (disbursed loans cannot be deleted)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Application deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete disbursed loan"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @Parameter(description = "Application ID") @PathVariable Long id) {
        log.info("Deleting loan application with ID: {}", id);
        loanApplicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
