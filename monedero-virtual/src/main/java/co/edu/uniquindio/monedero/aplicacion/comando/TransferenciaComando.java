package co.edu.uniquindio.monedero.aplicacion.comando;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaComando {
    private String cedulaClienteOrigen;
    private String cedulaClienteDestino;
    private double monto;
    private String descripcion; // Campo opcional para describir el propósito de la transferencia

    public TransferenciaComando(String cedulaClienteOrigen, String cedulaClienteDestino, double monto) {
        this.cedulaClienteOrigen = cedulaClienteOrigen;
        this.cedulaClienteDestino = cedulaClienteDestino;
        this.monto = monto;
    }
} 