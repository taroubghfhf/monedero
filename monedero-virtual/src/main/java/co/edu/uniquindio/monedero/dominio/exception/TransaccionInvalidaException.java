package co.edu.uniquindio.monedero.dominio.exception;

public class TransaccionInvalidaException extends RuntimeException {

    public TransaccionInvalidaException(String message) {
        super(message);
    }
    public TransaccionInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
