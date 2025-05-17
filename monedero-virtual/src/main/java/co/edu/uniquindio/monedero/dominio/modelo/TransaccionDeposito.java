package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class TransaccionDeposito extends Transaccion {
    private double comision;
    private int numeroDePuntos;


    public TransaccionDeposito(double monto) {
        super(monto, TipoTransaccion.DEPOSITO);
        this.comision = 0.0;
        this.numeroDePuntos = 0;
    }
}