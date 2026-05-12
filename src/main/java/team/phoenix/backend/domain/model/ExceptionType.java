package team.phoenix.backend.domain.model;

// Tipos de excepções mensais que afetam o cálculo de comissão
public enum ExceptionType {
    ABSENCE,
    VACATION,
    MATERNITY_LEAVE,
    BONUS_FIXED,
    RATE_OVERRIDE,
    SALES_BONUS_TIER,
    STORE_BONUS_TIER,
    MULTI_STORE
}
