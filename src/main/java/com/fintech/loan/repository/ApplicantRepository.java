package com.fintech.loan.repository;

import com.fintech.loan.entity.Applicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for Applicant entities.
 *
 * <p>Provides data access operations for loan applicants including
 * custom queries for email lookup and filtering by name or state.</p>
 *
 * @see Applicant
 */
@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    Optional<Applicant> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Applicant> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    Page<Applicant> findByState(String state, Pageable pageable);
}
