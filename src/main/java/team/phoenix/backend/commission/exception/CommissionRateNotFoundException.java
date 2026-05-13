package team.phoenix.backend.commission.exception;

public class CommissionRateNotFoundException extends RuntimeException {
    public CommissionRateNotFoundException(String message) {
        super(message);
    }
}
