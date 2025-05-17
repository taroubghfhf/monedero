package co.edu.uniquindio.monedero.dominio.exception;

public class NoExisteClienteException extends RuntimeException {

    public NoExisteClienteException(String mensaje) {
        super(mensaje);
    }

    public NoExisteClienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
