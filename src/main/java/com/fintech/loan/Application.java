package com.fintech.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Loan Origination Credit API application.
 *
 * <p>This Spring Boot application provides a RESTful API for managing loan applications,
 * applicants, and credit assessments. It includes features such as:</p>
 * <ul>
 *   <li>Applicant management with personal and financial information</li>
 *   <li>Loan application lifecycle management</li>
 *   <li>Automated credit scoring based on multiple factors</li>
 *   <li>Comprehensive audit logging for compliance</li>
 * </ul>
 *
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class Application {

    /**
     * Starts the Loan Origination Credit API application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
