package com.openclaw.loanorigination.controller;

import com.openclaw.loanorigination.dto.ApplicantDTO;
import com.openclaw.loanorigination.service.ApplicantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing loan applicants.
 *
 * <p>Provides CRUD operations for applicant records including personal information,
 * employment details, and financial data used in credit assessments.</p>
 */
@RestController
@RequestMapping("/api/v1/applicants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Applicants", description = "Operations for managing loan applicants")
public class ApplicantController {

    private final ApplicantService applicantService;

    /**
     * Creates a new applicant with the provided information.
     *
     * @param request the applicant creation request containing all required fields
     * @return the created applicant with assigned ID
     */
    @Operation(summary = "Create a new applicant", description = "Registers a new loan applicant in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Applicant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Applicant with email already exists")
    })
    @PostMapping
    public ResponseEntity<ApplicantDTO.Response> createApplicant(
            @Valid @RequestBody ApplicantDTO.CreateRequest request) {
        log.info("Creating applicant with email: {}", request.getEmail());
        ApplicantDTO.Response response = applicantService.createApplicant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an applicant by their unique identifier.
     *
     * @param id the applicant ID
     * @return the applicant details
     */
    @Operation(summary = "Get applicant by ID", description = "Retrieves a specific applicant using their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applicant found"),
        @ApiResponse(responseCode = "404", description = "Applicant not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicantDTO.Response> getApplicant(
            @Parameter(description = "Applicant ID") @PathVariable Long id) {
        log.debug("Fetching applicant with ID: {}", id);
        ApplicantDTO.Response response = applicantService.getApplicant(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all applicants with pagination support.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return a paginated list of applicants
     */
    @Operation(summary = "List all applicants", description = "Retrieves a paginated list of all applicants")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<ApplicantDTO.Response>> listApplicants(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("Listing applicants: page={}, size={}", page, size);
        Page<ApplicantDTO.Response> response = applicantService.listApplicants(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing applicant's information.
     *
     * @param id the applicant ID
     * @param request the update request containing fields to modify
     * @return the updated applicant
     */
    @Operation(summary = "Update an applicant", description = "Updates an existing applicant's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applicant updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Applicant not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApplicantDTO.Response> updateApplicant(
            @Parameter(description = "Applicant ID") @PathVariable Long id,
            @Valid @RequestBody ApplicantDTO.UpdateRequest request) {
        log.info("Updating applicant with ID: {}", id);
        ApplicantDTO.Response response = applicantService.updateApplicant(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an applicant by their ID.
     *
     * @param id the applicant ID
     * @return no content on successful deletion
     */
    @Operation(summary = "Delete an applicant", description = "Removes an applicant from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Applicant deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Applicant not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplicant(
            @Parameter(description = "Applicant ID") @PathVariable Long id) {
        log.info("Deleting applicant with ID: {}", id);
        applicantService.deleteApplicant(id);
        return ResponseEntity.noContent().build();
    }
}
