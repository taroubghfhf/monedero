package co.edu.uniquindio.monedero.dominio.exception;

public class YaExisteCuentaException extends RuntimeException {

    public YaExisteCuentaException(String message) {
        super(message);
    }

    public YaExisteCuentaException(String message, Throwable cause) {
        super(message, cause);
    }
}
