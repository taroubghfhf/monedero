package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {
    private String id;
    private double monto;
    private LocalDateTime fecha;
    private TipoTransaccion tipoTransaccion;

    public Transaccion(double monto, TipoTransaccion tipoTransaccion) {
        this.monto = monto;
        this.tipoTransaccion = tipoTransaccion;
        this.fecha = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
    }
}
