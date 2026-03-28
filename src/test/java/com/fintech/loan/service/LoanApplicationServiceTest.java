package com.fintech.loan.service;

import com.fintech.loan.dto.LoanApplicationDTO;
import com.fintech.loan.entity.Applicant;
import com.fintech.loan.entity.LoanApplication;
import com.fintech.loan.exception.BusinessException;
import com.fintech.loan.exception.ResourceNotFoundException;
import com.fintech.loan.repository.ApplicantRepository;
import com.fintech.loan.repository.LoanApplicationRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private ApplicantService applicantService;

    @Mock
    private CreditScoringService creditScoringService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RequestContextUtil requestContextUtil;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private LoanApplication testApplication;
    private Applicant testApplicant;
    private LoanApplicationDTO.CreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testApplicant = new Applicant();
        testApplicant.setId(1L);
        testApplicant.setFirstName("John");
        testApplicant.setLastName("Doe");
        testApplicant.setEmail("john.doe.test@example.com");

        testApplication = new LoanApplication();
        testApplication.setId(1L);
        testApplication.setApplicant(testApplicant);
        testApplication.setRequestedAmount(new BigDecimal("50000.00"));
        testApplication.setLoanTermMonths(36);
        testApplication.setLoanPurpose("Home improvement");
        testApplication.setStatus(LoanApplication.ApplicationStatus.DRAFT);

        createRequest = new LoanApplicationDTO.CreateRequest();
        createRequest.setApplicantId(1L);
        createRequest.setRequestedAmount(new BigDecimal("50000.00"));
        createRequest.setLoanTermMonths(36);
        createRequest.setLoanPurpose("Home improvement");

        lenient().when(requestContextUtil.getCurrentActor()).thenReturn("system");
        lenient().when(requestContextUtil.getClientIp()).thenReturn("127.0.0.1");
    }

    @Test
    @DisplayName("createApplication - valid request - returns LoanApplicationDTO")
    void createApplication_validRequest_returnsLoanApplicationDTO() {
        when(applicantService.getEntityById(1L)).thenReturn(testApplicant);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(testApplication);

        LoanApplicationDTO.Response response = loanApplicationService.createApplication(createRequest);

        assertNotNull(response);
        assertEquals(new BigDecimal("50000.00"), response.getRequestedAmount());
        assertEquals(36, response.getLoanTermMonths());
        assertEquals("Home improvement", response.getLoanPurpose());
        assertEquals("DRAFT", response.getStatus());
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("createApplication - non-existent applicant - throws ResourceNotFoundException")
    void createApplication_nonExistentApplicant_throwsException() {
        when(applicantService.getEntityById(999L)).thenThrow(new ResourceNotFoundException("Applicant not found"));
        createRequest.setApplicantId(999L);

        assertThrows(ResourceNotFoundException.class, () -> {
            loanApplicationService.createApplication(createRequest);
        });
    }

    @Test
    @DisplayName("getApplication - existing id - returns LoanApplicationDTO")
    void getApplication_existingId_returnsLoanApplicationDTO() {
        when(loanApplicationRepository.findByIdWithApplicantAndAssessment(1L)).thenReturn(Optional.of(testApplication));

        LoanApplicationDTO.Response response = loanApplicationService.getApplication(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("DRAFT", response.getStatus());
    }

    @Test
    @DisplayName("getApplication - non-existent id - throws ResourceNotFoundException")
    void getApplication_nonExistentId_throwsResourceNotFoundException() {
        when(loanApplicationRepository.findByIdWithApplicantAndAssessment(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            loanApplicationService.getApplication(999L);
        });
    }

    @Test
    @DisplayName("submitApplication - draft application - returns submitted application")
    void submitApplication_draftApplication_returnsSubmittedApplication() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(invocation -> {
            LoanApplication app = invocation.getArgument(0);
            app.setStatus(LoanApplication.ApplicationStatus.SUBMITTED);
            return app;
        });

        LoanApplicationDTO.Response response = loanApplicationService.submitApplication(1L);

        assertNotNull(response);
        assertEquals("SUBMITTED", response.getStatus());
    }

    @Test
    @DisplayName("submitApplication - non-draft application - throws BusinessException")
    void submitApplication_nonDraftApplication_throwsBusinessException() {
        testApplication.setStatus(LoanApplication.ApplicationStatus.SUBMITTED);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        assertThrows(BusinessException.class, () -> {
            loanApplicationService.submitApplication(1L);
        });
    }

    @Test
    @DisplayName("listApplications - returns paginated list")
    void listApplications_returnsPaginatedList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LoanApplication> applicationPage = new PageImpl<>(List.of(testApplication), pageable, 1);

        when(loanApplicationRepository.findAll(any(Pageable.class))).thenReturn(applicationPage);

        LoanApplicationDTO.ListResponse response = loanApplicationService.listApplications(null, null, 0, 20);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }

    @Test
    @DisplayName("deleteApplication - existing id - deletes successfully")
    void deleteApplication_existingId_deletesSuccessfully() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        doNothing().when(loanApplicationRepository).delete(any(LoanApplication.class));

        loanApplicationService.deleteApplication(1L);

        verify(loanApplicationRepository).delete(any(LoanApplication.class));
    }
}
