package com.fintech.loan.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Objects for Applicant operations.
 *
 * <p>Contains request and response DTOs for creating, updating, and retrieving applicants.</p>
 */
public class ApplicantDTO {

    /**
     * Request DTO for creating a new applicant.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "First name is required")
        @Size(max = 50)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 50)
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        private String phoneNumber;

        @NotBlank(message = "SSN is required")
        @Pattern(regexp = "^[0-9]{3}-[0-9]{2}-[0-9]{4}$", message = "SSN must be XXX-XX-XXXX format")
        private String ssn;

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        private LocalDate dateOfBirth;

        @NotBlank(message = "Address is required")
        @Size(max = 500)
        private String address;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        @Size(min = 2, max = 2, message = "State must be 2-letter code")
        private String state;

        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zip code")
        private String zipCode;

        @NotNull(message = "Annual income is required")
        @Positive(message = "Annual income must be positive")
        private BigDecimal annualIncome;

        @NotBlank(message = "Employer name is required")
        private String employerName;

        @NotNull(message = "Employment years is required")
        @PositiveOrZero(message = "Employment years must be 0 or positive")
        private Integer employmentYears;

        @NotNull(message = "Credit history years is required")
        @PositiveOrZero(message = "Credit history years must be 0 or positive")
        private Integer creditHistoryYears;

        @NotNull(message = "Total debt is required")
        @PositiveOrZero(message = "Total debt must be 0 or positive")
        private BigDecimal totalDebt;
    }

    /**
     * Request DTO for updating an existing applicant.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 50)
        private String firstName;

        @Size(max = 50)
        private String lastName;

        @Email(message = "Invalid email format")
        private String email;

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        private String phoneNumber;

        @Size(max = 500)
        private String address;

        private String city;

        @Size(min = 2, max = 2, message = "State must be 2-letter code")
        private String state;

        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zip code")
        private String zipCode;

        @Positive(message = "Annual income must be positive")
        private BigDecimal annualIncome;

        private String employerName;

        @PositiveOrZero(message = "Employment years must be 0 or positive")
        private Integer employmentYears;

        @PositiveOrZero(message = "Credit history years must be 0 or positive")
        private Integer creditHistoryYears;

        @PositiveOrZero(message = "Total debt must be 0 or positive")
        private BigDecimal totalDebt;
    }

    /**
     * Response DTO for applicant data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private BigDecimal annualIncome;
        private String employerName;
        private Integer employmentYears;
        private Integer creditHistoryYears;
        private BigDecimal totalDebt;
        private Long version;
        private String createdAt;
        private String updatedAt;
    }
}
