package co.edu.uniquindio.monedero.aplicacion.comando;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClienteComando {
    private String nombre;
    private String apellido;
    private String cedula;
    private String correo;
    private String telefono;

    public ClienteComando(String nombre, String apellido, String cedula, String correo, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.correo = correo;
        this.telefono = telefono;
    }

}
