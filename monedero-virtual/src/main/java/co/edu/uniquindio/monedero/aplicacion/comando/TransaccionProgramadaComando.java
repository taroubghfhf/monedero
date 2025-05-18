package co.edu.uniquindio.monedero.aplicacion.comando;

import co.edu.uniquindio.monedero.dominio.modelo.Periodicidad;
import co.edu.uniquindio.monedero.dominio.modelo.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionProgramadaComando {
    private String clienteOrigen;
    private String clienteDestino;
    private double monto;
    private TipoTransaccion tipoTransaccion;
    private LocalDateTime fechaEjecucion;
    private Periodicidad periodicidad;
} 