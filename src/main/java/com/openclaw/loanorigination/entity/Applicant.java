package com.openclaw.loanorigination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity representing a loan applicant.
 *
 * <p>Stores personal information, employment details, and financial data
 * used in credit assessments.</p>
 */
@Entity
@Table(name = "applicants", indexes = {
    @Index(name = "idx_applicant_email", columnList = "email"),
    @Index(name = "idx_applicant_last_name", columnList = "lastName"),
    @Index(name = "idx_applicant_state", columnList = "state")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String ssn; // Encrypted in production

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal annualIncome;

    @Column(nullable = false)
    private String employerName;

    @Column(nullable = false)
    private Integer employmentYears;

    @Column(nullable = false)
    private Integer creditHistoryYears;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDebt;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
