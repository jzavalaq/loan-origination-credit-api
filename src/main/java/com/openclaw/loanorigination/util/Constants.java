package com.openclaw.loanorigination.util;

/**
 * Application-wide constants.
 * Centralizes magic strings and numbers for better maintainability.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_NUMBER = 0;

    // Actor/audit defaults
    public static final String SYSTEM_ACTOR = "system";

    // HTTP Headers
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String HEADER_X_CORRELATION_ID = "X-Correlation-ID";

    // Credit Scoring thresholds
    public static final int CREDIT_SCORE_MIN = 300;
    public static final int CREDIT_SCORE_MAX = 850;
    public static final int CREDIT_SCORE_BASE = 300;
    public static final int APPROVE_THRESHOLD = 700;
    public static final int MANUAL_REVIEW_THRESHOLD = 600;

    // Credit Scoring weights
    public static final double INCOME_WEIGHT = 0.30;
    public static final double EMPLOYMENT_WEIGHT = 0.20;
    public static final double CREDIT_HISTORY_WEIGHT = 0.25;
    public static final double DEBT_WEIGHT = 0.25;

    // Validation constraints
    public static final int MAX_FIRST_NAME_LENGTH = 50;
    public static final int MAX_LAST_NAME_LENGTH = 50;
    public static final int MAX_ADDRESS_LENGTH = 500;
    public static final int MAX_PURPOSE_LENGTH = 200;
    public static final int MAX_DECISION_REASON_LENGTH = 500;
}
