package team.phoenix.backend.commission.exception;

public class InvalidCommissionRequestException extends IllegalArgumentException {
    public InvalidCommissionRequestException(String message) {
        super(message);
    }
}
