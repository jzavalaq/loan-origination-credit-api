package com.openclaw.loanorigination.service;

import com.openclaw.loanorigination.dto.ApplicantDTO;
import com.openclaw.loanorigination.entity.Applicant;
import com.openclaw.loanorigination.exception.DuplicateResourceException;
import com.openclaw.loanorigination.exception.ResourceNotFoundException;
import com.openclaw.loanorigination.repository.ApplicantRepository;
import com.openclaw.loanorigination.util.Constants;
import com.openclaw.loanorigination.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing loan applicants.
 *
 * <p>Handles CRUD operations for applicants including creation with duplicate detection,
 * retrieval with pagination support, updates with selective field modification,
 * and deletion with audit logging.</p>
 *
 * @see Applicant
 * @see ApplicantRepository
 * @see AuditLogService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final AuditLogService auditLogService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Creates a new applicant with the provided information.
     *
     * @param dto the applicant creation request
     * @return the created applicant response
     * @throws DuplicateResourceException if an applicant with the same email already exists
     */
    @Transactional
    public ApplicantDTO.Response createApplicant(ApplicantDTO.CreateRequest dto) {
        if (applicantRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Applicant with email " + dto.getEmail() + " already exists");
        }

        Applicant applicant = Applicant.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .ssn(dto.getSsn())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .annualIncome(dto.getAnnualIncome())
                .employerName(dto.getEmployerName())
                .employmentYears(dto.getEmploymentYears())
                .creditHistoryYears(dto.getCreditHistoryYears())
                .totalDebt(dto.getTotalDebt())
                .build();

        Applicant saved = applicantRepository.save(applicant);

        auditLogService.log(
                "APPLICANT", saved.getId(), "CREATE",
                null, saved, getCurrentActor(), getClientIp()
        );

        log.info("Created applicant {} with ID {}", saved.getEmail(), saved.getId());
        return toResponse(saved);
    }

    /**
     * Retrieves an applicant by their unique identifier.
     *
     * @param id the applicant ID
     * @return the applicant response
     * @throws ResourceNotFoundException if applicant is not found
     */
    @Transactional(readOnly = true)
    public ApplicantDTO.Response getApplicant(Long id) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found with id: " + id));
        return toResponse(applicant);
    }

    /**
     * Lists applicants with pagination support.
     *
     * @param page the page number (0-indexed)
     * @param size the page size (max 100)
     * @return paginated list of applicants
     */
    @Transactional(readOnly = true)
    public Page<ApplicantDTO.Response> listApplicants(int page, int size) {
        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        if (page < Constants.MIN_PAGE_NUMBER) page = Constants.MIN_PAGE_NUMBER;
        if (safeSize < 1) safeSize = Constants.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return applicantRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Updates an existing applicant's information.
     *
     * @param id the applicant ID
     * @param dto the update request containing fields to modify
     * @return the updated applicant response
     * @throws ResourceNotFoundException if applicant is not found
     */
    @Transactional
    public ApplicantDTO.Response updateApplicant(Long id, ApplicantDTO.UpdateRequest dto) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found with id: " + id));

        Applicant oldApplicant = toApplicantCopy(applicant);

        if (dto.getFirstName() != null) applicant.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) applicant.setLastName(dto.getLastName());
        if (dto.getEmail() != null) applicant.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) applicant.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddress() != null) applicant.setAddress(dto.getAddress());
        if (dto.getCity() != null) applicant.setCity(dto.getCity());
        if (dto.getState() != null) applicant.setState(dto.getState());
        if (dto.getZipCode() != null) applicant.setZipCode(dto.getZipCode());
        if (dto.getAnnualIncome() != null) applicant.setAnnualIncome(dto.getAnnualIncome());
        if (dto.getEmployerName() != null) applicant.setEmployerName(dto.getEmployerName());
        if (dto.getEmploymentYears() != null) applicant.setEmploymentYears(dto.getEmploymentYears());
        if (dto.getCreditHistoryYears() != null) applicant.setCreditHistoryYears(dto.getCreditHistoryYears());
        if (dto.getTotalDebt() != null) applicant.setTotalDebt(dto.getTotalDebt());

        Applicant saved = applicantRepository.save(applicant);

        auditLogService.log(
                "APPLICANT", saved.getId(), "UPDATE",
                oldApplicant, saved, getCurrentActor(), getClientIp()
        );

        log.info("Updated applicant with ID {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Deletes an applicant by their ID.
     *
     * @param id the applicant ID
     * @throws ResourceNotFoundException if applicant is not found
     */
    @Transactional
    public void deleteApplicant(Long id) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found with id: " + id));

        auditLogService.log(
                "APPLICANT", applicant.getId(), "DELETE",
                applicant, null, getCurrentActor(), getClientIp()
        );

        applicantRepository.delete(applicant);
        log.info("Deleted applicant with ID {}", id);
    }

    /**
     * Retrieves an applicant entity by ID for internal use.
     *
     * @param id the applicant ID
     * @return the applicant entity
     * @throws ResourceNotFoundException if applicant is not found
     */
    @Transactional(readOnly = true)
    public Applicant getEntityById(Long id) {
        return applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found with id: " + id));
    }

    /**
     * Converts an Applicant entity to a response DTO.
     *
     * @param applicant the applicant entity
     * @return the response DTO
     */
    private ApplicantDTO.Response toResponse(Applicant applicant) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return ApplicantDTO.Response.builder()
                .id(applicant.getId())
                .firstName(applicant.getFirstName())
                .lastName(applicant.getLastName())
                .email(applicant.getEmail())
                .phoneNumber(applicant.getPhoneNumber())
                .dateOfBirth(applicant.getDateOfBirth())
                .address(applicant.getAddress())
                .city(applicant.getCity())
                .state(applicant.getState())
                .zipCode(applicant.getZipCode())
                .annualIncome(applicant.getAnnualIncome())
                .employerName(applicant.getEmployerName())
                .employmentYears(applicant.getEmploymentYears())
                .creditHistoryYears(applicant.getCreditHistoryYears())
                .totalDebt(applicant.getTotalDebt())
                .version(applicant.getVersion())
                .createdAt(applicant.getCreatedAt() != null ? formatter.format(applicant.getCreatedAt()) : null)
                .updatedAt(applicant.getUpdatedAt() != null ? formatter.format(applicant.getUpdatedAt()) : null)
                .build();
    }

    /**
     * Creates a copy of an Applicant entity for audit logging.
     *
     * @param source the source applicant
     * @return a copy of the applicant without ID and timestamps
     */
    private Applicant toApplicantCopy(Applicant source) {
        return Applicant.builder()
                .id(source.getId())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .email(source.getEmail())
                .phoneNumber(source.getPhoneNumber())
                .ssn(source.getSsn())
                .dateOfBirth(source.getDateOfBirth())
                .address(source.getAddress())
                .city(source.getCity())
                .state(source.getState())
                .zipCode(source.getZipCode())
                .annualIncome(source.getAnnualIncome())
                .employerName(source.getEmployerName())
                .employmentYears(source.getEmploymentYears())
                .creditHistoryYears(source.getCreditHistoryYears())
                .totalDebt(source.getTotalDebt())
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
