package co.edu.uniquindio.monedero.dominio.exception;

public class CuentaNoExisteException extends RuntimeException{
    public CuentaNoExisteException(String message) {
        super(message);
    }

    public CuentaNoExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}
