package co.edu.uniquindio.monedero.dominio.exception;

public class ClienteYaExisteException extends RuntimeException{

    public ClienteYaExisteException(String mensaje) {
        super(mensaje);
    }
    public ClienteYaExisteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
