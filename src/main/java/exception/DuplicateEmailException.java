package exception;

public class DuplicateEmailException extends Throwable {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
