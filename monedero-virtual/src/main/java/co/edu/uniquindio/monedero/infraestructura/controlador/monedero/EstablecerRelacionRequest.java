package co.edu.uniquindio.monedero.infraestructura.controlador.monedero;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstablecerRelacionRequest {
    private String cedulaCliente;
    private String idOrigen;
    private String idDestino;
    private double comision;
} 