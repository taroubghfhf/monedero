package co.edu.uniquindio.monedero.infraestructura.controlador.monedero;

import co.edu.uniquindio.monedero.dominio.modelo.TipoMonedero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearMonederoRequest {
    private String cedulaCliente;
    private String nombre;
    private TipoMonedero tipo;
} 