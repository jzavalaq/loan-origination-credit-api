-- V1__init.sql - Initial schema for loan origination system

-- Applicants table
CREATE TABLE applicants (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    ssn VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    zip_code VARCHAR(255) NOT NULL,
    annual_income NUMERIC(12, 2) NOT NULL,
    employer_name VARCHAR(255) NOT NULL,
    employment_years INTEGER NOT NULL,
    credit_history_years INTEGER NOT NULL,
    total_debt NUMERIC(12, 2) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Indexes for applicants
CREATE INDEX idx_applicant_email ON applicants(email);
CREATE INDEX idx_applicant_last_name ON applicants(last_name);
CREATE INDEX idx_applicant_state ON applicants(state);

-- Loan applications table
CREATE TABLE loan_applications (
    id BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    requested_amount NUMERIC(12, 2) NOT NULL,
    loan_term_months INTEGER NOT NULL,
    loan_purpose VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    decision VARCHAR(50),
    decision_reason VARCHAR(255),
    assessment_id BIGINT UNIQUE,
    version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    submitted_at TIMESTAMP WITH TIME ZONE,
    decided_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_loan_applicant FOREIGN KEY (applicant_id) REFERENCES applicants(id)
);

-- Indexes for loan_applications
CREATE INDEX idx_loan_applicant_id ON loan_applications(applicant_id);
CREATE INDEX idx_loan_status ON loan_applications(status);
CREATE INDEX idx_loan_decision ON loan_applications(decision);
CREATE INDEX idx_loan_created_at ON loan_applications(created_at);

-- Credit assessments table
CREATE TABLE credit_assessments (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    credit_score INTEGER NOT NULL,
    debt_to_income_ratio NUMERIC(5, 2) NOT NULL,
    income_score INTEGER NOT NULL,
    employment_score INTEGER NOT NULL,
    credit_history_score INTEGER NOT NULL,
    debt_score INTEGER NOT NULL,
    score_factors VARCHAR(1000) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes for credit_assessments
CREATE INDEX idx_credit_assessment_application_id ON credit_assessments(application_id);
CREATE INDEX idx_credit_assessment_risk_level ON credit_assessments(risk_level);
CREATE INDEX idx_credit_assessment_credit_score ON credit_assessments(credit_score);

-- Add foreign key from loan_applications to credit_assessments
ALTER TABLE loan_applications ADD CONSTRAINT fk_loan_assessment FOREIGN KEY (assessment_id) REFERENCES credit_assessments(id);

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    old_value VARCHAR(2000),
    new_value VARCHAR(2000) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes for audit_logs
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_actor ON audit_logs(actor);
