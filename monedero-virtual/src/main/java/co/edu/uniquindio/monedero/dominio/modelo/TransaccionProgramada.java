package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TransaccionProgramada extends Transaccion implements Comparable<TransaccionProgramada> {
    private String id;
    private String clienteOrigen;
    private String clienteDestino;
    private LocalDateTime fechaEjecucion;
    private Periodicidad periodicidad;
    private boolean activa = true;

    public TransaccionProgramada(String clienteOrigen, String clienteDestino,
                               double monto, TipoTransaccion tipoTransaccion,
                               LocalDateTime fechaEjecucion, Periodicidad periodicidad) {
        super(monto, tipoTransaccion);
        this.id = UUID.randomUUID().toString();
        this.clienteOrigen = clienteOrigen;
        this.clienteDestino = clienteDestino;
        this.fechaEjecucion = fechaEjecucion;
        this.periodicidad = periodicidad;
    }

    @Override
    public int compareTo(TransaccionProgramada otra) {
        return this.fechaEjecucion.compareTo(otra.fechaEjecucion);
    }
} 