package exceptions;

public class InvalidFlightStatusException extends Exception {
    public InvalidFlightStatusException(String message) {
        super(message);
    }
}
