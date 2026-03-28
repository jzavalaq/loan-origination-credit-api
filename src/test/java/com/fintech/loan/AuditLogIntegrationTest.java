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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test-user", roles = {"USER", "ADMIN"})
class AuditLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void queryAuditLogs_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    void queryAuditLogs_withFilters_shouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                .param("entityType", "APPLICANT")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }
}
