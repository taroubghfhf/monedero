package co.edu.uniquindio.monedero.dominio.dto.transferencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoTransferenciaDTO {
    private boolean exitoso;
    private String mensaje;
    private double monto;
    private String cedulaClienteOrigen;
    private String cedulaClienteDestino;
} 