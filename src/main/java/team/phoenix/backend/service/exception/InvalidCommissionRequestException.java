package team.phoenix.backend.service.exception;

public class InvalidCommissionRequestException extends IllegalArgumentException {
    public InvalidCommissionRequestException(String message) {
        super(message);
    }
}
