package co.edu.uniquindio.monedero.infraestructura.error;

public class Error {

    private String nombreExcepcion;
    private String mensaje;
    private String message; // For consistency with standard error formats

    public Error(String nombreExcepcion, String mensaje) {
        this.nombreExcepcion = nombreExcepcion;
        this.mensaje = mensaje;
        this.message = mensaje; // Set both fields for compatibility
    }

    public String getNombreExcepcion() {
        return nombreExcepcion;
    }

    public String getMensaje() {
        return mensaje;
    }
    
    public String getMessage() {
        return message;
    }
}