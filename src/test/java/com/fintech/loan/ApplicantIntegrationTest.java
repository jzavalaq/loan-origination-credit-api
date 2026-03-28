package com.fintech.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "test-user", roles = {"USER", "ADMIN"})
class ApplicantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createApplicant_shouldReturn201AndApplicant() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "John");
        request.put("lastName", "Doe");
        request.put("email", "john.doe.test@example.com");
        request.put("phoneNumber", "+1234567890");
        request.put("ssn", "123-45-6789");
        request.put("dateOfBirth", "1990-01-15");
        request.put("address", "123 Main Street");
        request.put("city", "New York");
        request.put("state", "NY");
        request.put("zipCode", "10001");
        request.put("annualIncome", 75000.00);
        request.put("employerName", "Acme Corp");
        request.put("employmentYears", 5);
        request.put("creditHistoryYears", 7);
        request.put("totalDebt", 15000.00);

        mockMvc.perform(post("/api/v1/applicants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe.test@example.com")));
    }

    @Test
    void createApplicant_withInvalidEmail_shouldReturn400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Jane");
        request.put("lastName", "Smith");
        request.put("email", "invalid-email");
        request.put("phoneNumber", "+1234567890");
        request.put("ssn", "123-45-6789");
        request.put("dateOfBirth", "1985-05-20");
        request.put("address", "456 Oak Ave");
        request.put("city", "Los Angeles");
        request.put("state", "CA");
        request.put("zipCode", "90001");
        request.put("annualIncome", 85000.00);
        request.put("employerName", "Tech Inc");
        request.put("employmentYears", 3);
        request.put("creditHistoryYears", 10);
        request.put("totalDebt", 20000.00);

        mockMvc.perform(post("/api/v1/applicants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getApplicant_shouldReturnApplicant() throws Exception {
        // First create an applicant
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("firstName", "Test");
        createRequest.put("lastName", "User");
        createRequest.put("email", "test.user.get@example.com");
        createRequest.put("phoneNumber", "+1555555555");
        createRequest.put("ssn", "999-99-9999");
        createRequest.put("dateOfBirth", "1988-03-10");
        createRequest.put("address", "789 Test Blvd");
        createRequest.put("city", "Chicago");
        createRequest.put("state", "IL");
        createRequest.put("zipCode", "60601");
        createRequest.put("annualIncome", 95000.00);
        createRequest.put("employerName", "Test Co");
        createRequest.put("employmentYears", 8);
        createRequest.put("creditHistoryYears", 12);
        createRequest.put("totalDebt", 10000.00);

        MvcResult result = mockMvc.perform(post("/api/v1/applicants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long applicantId = objectMapper.readTree(response).get("id").asLong();

        // Then get the applicant
        mockMvc.perform(get("/api/v1/applicants/{id}", applicantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(applicantId.intValue())))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")));
    }

    @Test
    void getApplicant_withNonExistentId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/applicants/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void listApplicants_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/v1/applicants")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }
}
