package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransaccionMonedero extends Transaccion {
    private String nombre;

    public TransaccionMonedero(double monto, String id, LocalDateTime fecha, String nombre) {
        super(monto, id, fecha);
        this.nombre = nombre;
    }
}
