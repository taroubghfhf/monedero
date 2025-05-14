package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransaccionTransferencia extends Transaccion {
    private double comision;
    private int numeroDePuntos;

    public TransaccionTransferencia(double monto, String id, LocalDateTime fecha, double comision, int numeroDePuntos) {
        super(monto, id, fecha);
        this.comision = comision;
        this.numeroDePuntos = numeroDePuntos;
    }
}
