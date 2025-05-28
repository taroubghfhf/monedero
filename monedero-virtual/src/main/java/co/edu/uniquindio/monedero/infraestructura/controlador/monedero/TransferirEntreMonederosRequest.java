package co.edu.uniquindio.monedero.infraestructura.controlador.monedero;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferirEntreMonederosRequest {
    private String cedulaCliente;
    private String idMonederoOrigen;
    private String idMonederoDestino;
    private double monto;
    private String concepto;
} 