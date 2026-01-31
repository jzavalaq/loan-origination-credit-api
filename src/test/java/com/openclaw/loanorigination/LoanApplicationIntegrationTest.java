package com.openclaw.loanorigination;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "test-user", roles = {"USER", "ADMIN"})
class LoanApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long applicantId;

    @BeforeEach
    void setUp() throws Exception {
        // Create an applicant for testing
        Map<String, Object> applicantRequest = new HashMap<>();
        applicantRequest.put("firstName", "Loan");
        applicantRequest.put("lastName", "Applicant");
        applicantRequest.put("email", "loan.applicant." + UUID.randomUUID().toString() + "@example.com");
        applicantRequest.put("phoneNumber", "+1444444444");
        applicantRequest.put("ssn", "444-44-4444");
        applicantRequest.put("dateOfBirth", "1985-07-25");
        applicantRequest.put("address", "100 Loan Street");
        applicantRequest.put("city", "Miami");
        applicantRequest.put("state", "FL");
        applicantRequest.put("zipCode", "33101");
        applicantRequest.put("annualIncome", 120000.00);
        applicantRequest.put("employerName", "Finance Corp");
        applicantRequest.put("employmentYears", 7);
        applicantRequest.put("creditHistoryYears", 15);
        applicantRequest.put("totalDebt", 25000.00);

        MvcResult result = mockMvc.perform(post("/api/v1/applicants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicantRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        applicantId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createApplication_shouldReturn201AndApplication() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("applicantId", applicantId);
        request.put("requestedAmount", 50000.00);
        request.put("loanTermMonths", 36);
        request.put("loanPurpose", "Home improvement");

        mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.applicantId", is(applicantId.intValue())))
                .andExpect(jsonPath("$.requestedAmount", is(50000.00)))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }

    @Test
    void createApplication_withInvalidAmount_shouldReturn400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("applicantId", applicantId);
        request.put("requestedAmount", 500.00); // Below minimum
        request.put("loanTermMonths", 36);
        request.put("loanPurpose", "Test");

        mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitAndAssessApplication_shouldReturnDecidedApplication() throws Exception {
        // Create application
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("applicantId", applicantId);
        createRequest.put("requestedAmount", 75000.00);
        createRequest.put("loanTermMonths", 48);
        createRequest.put("loanPurpose", "Business expansion");

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long applicationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Submit application
        mockMvc.perform(post("/api/v1/applications/{id}/submit", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUBMITTED")));

        // Assess application
        mockMvc.perform(post("/api/v1/applications/{id}/assess", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DECIDED")))
                .andExpect(jsonPath("$.decision", notNullValue()))
                .andExpect(jsonPath("$.creditAssessment.creditScore", notNullValue()))
                .andExpect(jsonPath("$.creditAssessment.creditScore", greaterThanOrEqualTo(300)))
                .andExpect(jsonPath("$.creditAssessment.creditScore", lessThanOrEqualTo(850)));
    }

    @Test
    void getApplication_shouldReturnApplication() throws Exception {
        // Create application
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("applicantId", applicantId);
        createRequest.put("requestedAmount", 25000.00);
        createRequest.put("loanTermMonths", 24);
        createRequest.put("loanPurpose", "Car purchase");

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long applicationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Get application
        mockMvc.perform(get("/api/v1/applications/{id}", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(applicationId.intValue())))
                .andExpect(jsonPath("$.loanPurpose", is("Car purchase")));
    }

    @Test
    void listApplications_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/v1/applications")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    void overrideDecision_shouldUpdateDecision() throws Exception {
        // Create and submit application
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("applicantId", applicantId);
        createRequest.put("requestedAmount", 100000.00);
        createRequest.put("loanTermMonths", 60);
        createRequest.put("loanPurpose", "Real estate");

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long applicationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Submit and assess
        mockMvc.perform(post("/api/v1/applications/{id}/submit", applicationId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/applications/{id}/assess", applicationId))
                .andExpect(status().isOk());

        // Override decision
        Map<String, Object> overrideRequest = new HashMap<>();
        overrideRequest.put("decision", "APPROVED");
        overrideRequest.put("decisionReason", "Manual override - strong collateral");

        mockMvc.perform(post("/api/v1/applications/{id}/override", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overrideRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision", is("APPROVED")));
    }
}
