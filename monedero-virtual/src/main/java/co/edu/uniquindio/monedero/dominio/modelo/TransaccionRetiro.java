package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class TransaccionRetiro extends Transaccion {
    private double comision;
    private int numeroDePuntos;

    public TransaccionRetiro(double monto) {
        super(monto , TipoTransaccion.RETIRO);
        this.comision = 0.0;
        this.numeroDePuntos = 0;
    }

   
}
