package team.phoenix.backend.service.exception;

public class CommissionRateNotFoundException extends RuntimeException {
    public CommissionRateNotFoundException(String message) {
        super(message);
    }
}
