package com.fintech.loan.service;

import com.fintech.loan.dto.ApplicantDTO;
import com.fintech.loan.entity.Applicant;
import com.fintech.loan.exception.DuplicateResourceException;
import com.fintech.loan.exception.ResourceNotFoundException;
import com.fintech.loan.repository.ApplicantRepository;
import com.fintech.loan.util.RequestContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RequestContextUtil requestContextUtil;

    @InjectMocks
    private ApplicantService applicantService;

    private Applicant testApplicant;
    private ApplicantDTO.CreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testApplicant = new Applicant();
        testApplicant.setId(1L);
        testApplicant.setFirstName("John");
        testApplicant.setLastName("Doe");
        testApplicant.setEmail("john.doe.test@example.com");
        testApplicant.setPhoneNumber("+1234567890");
        testApplicant.setSsn("123-45-6789");
        testApplicant.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testApplicant.setAddress("123 Main Street");
        testApplicant.setCity("New York");
        testApplicant.setState("NY");
        testApplicant.setZipCode("10001");
        testApplicant.setAnnualIncome(new BigDecimal("75000.00"));
        testApplicant.setEmployerName("Acme Corp");
        testApplicant.setEmploymentYears(5);
        testApplicant.setCreditHistoryYears(7);
        testApplicant.setTotalDebt(new BigDecimal("15000.00"));

        createRequest = new ApplicantDTO.CreateRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe.test@example.com");
        createRequest.setPhoneNumber("+1234567890");
        createRequest.setSsn("123-45-6789");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
        createRequest.setAddress("123 Main Street");
        createRequest.setCity("New York");
        createRequest.setState("NY");
        createRequest.setZipCode("10001");
        createRequest.setAnnualIncome(new BigDecimal("75000.00"));
        createRequest.setEmployerName("Acme Corp");
        createRequest.setEmploymentYears(5);
        createRequest.setCreditHistoryYears(7);
        createRequest.setTotalDebt(new BigDecimal("15000.00"));

        lenient().when(requestContextUtil.getCurrentActor()).thenReturn("system");
        lenient().when(requestContextUtil.getClientIp()).thenReturn("127.0.0.1");
    }

    @Test
    @DisplayName("createApplicant - valid request - returns ApplicantDTO")
    void createApplicant_validRequest_returnsApplicantDTO() {
        when(applicantRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(applicantRepository.save(any(Applicant.class))).thenReturn(testApplicant);

        ApplicantDTO.Response response = applicantService.createApplicant(createRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe.test@example.com", response.getEmail());
        verify(applicantRepository).save(any(Applicant.class));
        verify(auditLogService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("createApplicant - duplicate email - throws DuplicateResourceException")
    void createApplicant_duplicateEmail_throwsException() {
        when(applicantRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            applicantService.createApplicant(createRequest);
        });

        verify(applicantRepository, never()).save(any());
    }

    @Test
    @DisplayName("getApplicant - existing id - returns ApplicantDTO")
    void getApplicant_existingId_returnsApplicantDTO() {
        when(applicantRepository.findById(1L)).thenReturn(Optional.of(testApplicant));

        ApplicantDTO.Response response = applicantService.getApplicant(1L);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("getApplicant - non-existent id - throws ResourceNotFoundException")
    void getApplicant_nonExistentId_throwsResourceNotFoundException() {
        when(applicantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            applicantService.getApplicant(999L);
        });
    }

    @Test
    @DisplayName("updateApplicant - valid request - returns updated ApplicantDTO")
    void updateApplicant_validRequest_returnsUpdatedApplicantDTO() {
        ApplicantDTO.UpdateRequest updateRequest = new ApplicantDTO.UpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");
        updateRequest.setPhoneNumber("+1234567891");
        updateRequest.setAddress("456 Oak Ave");
        updateRequest.setCity("Los Angeles");
        updateRequest.setState("CA");
        updateRequest.setZipCode("90001");
        updateRequest.setAnnualIncome(new BigDecimal("85000.00"));

        when(applicantRepository.findById(1L)).thenReturn(Optional.of(testApplicant));
        when(applicantRepository.save(any(Applicant.class))).thenReturn(testApplicant);

        ApplicantDTO.Response response = applicantService.updateApplicant(1L, updateRequest);

        assertNotNull(response);
        verify(applicantRepository).save(any(Applicant.class));
        verify(auditLogService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("deleteApplicant - existing id - deletes successfully")
    void deleteApplicant_existingId_deletesSuccessfully() {
        when(applicantRepository.findById(1L)).thenReturn(Optional.of(testApplicant));
        doNothing().when(applicantRepository).delete(any(Applicant.class));

        applicantService.deleteApplicant(1L);

        verify(applicantRepository).delete(any(Applicant.class));
        verify(auditLogService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("listApplicants - returns paginated list")
    void listApplicants_returnsPaginatedList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Applicant> applicantPage = new PageImpl<>(List.of(testApplicant), pageable, 1);

        when(applicantRepository.findAll(any(Pageable.class))).thenReturn(applicantPage);

        Page<ApplicantDTO.Response> response = applicantService.listApplicants(0, 20);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }
}
