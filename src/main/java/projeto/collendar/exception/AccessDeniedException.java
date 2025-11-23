package projeto.collendar.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException() {
        super("Você não tem permissão para realizar esta ação");
    }
}