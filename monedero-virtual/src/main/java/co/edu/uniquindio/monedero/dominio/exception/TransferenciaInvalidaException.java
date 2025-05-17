package co.edu.uniquindio.monedero.dominio.exception;

public class TransferenciaInvalidaException extends RuntimeException {
    public TransferenciaInvalidaException(String message) {
        super(message);
    }

    public TransferenciaInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
} 