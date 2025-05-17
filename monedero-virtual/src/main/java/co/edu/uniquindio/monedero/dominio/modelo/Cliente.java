package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private String nombre;
    private String apellido;
    private String cedula;
    private String correo;
    private String telefono;
    private Cuenta cuenta;

    public Cliente(String nombre, String apellido, String cedula, String correo, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.correo = correo;
        this.telefono = telefono;
    }

}