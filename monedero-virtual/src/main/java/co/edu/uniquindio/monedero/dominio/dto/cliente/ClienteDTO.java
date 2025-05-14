package co.edu.uniquindio.monedero.dominio.dto.cliente;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String nombre;
    private String apellido;
    private String cedula;
    private String correo;
    private String telefono;
}
