package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class TransaccionMonedero extends Transaccion {
    private String nombre;

    public TransaccionMonedero(double monto) {
        super(monto, TipoTransaccion.MONEDERO);
        this.nombre = nombre;
    }
}
