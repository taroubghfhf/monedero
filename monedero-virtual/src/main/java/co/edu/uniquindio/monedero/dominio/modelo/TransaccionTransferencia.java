package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class TransaccionTransferencia extends Transaccion {
    private double comision;
    private int numeroDePuntos;

    public TransaccionTransferencia(double monto) {
        super(monto, TipoTransaccion.TRANSFERENCIA_SALIENTE);
        this.comision = 0.0;
        this.numeroDePuntos = 0;
    }
}
