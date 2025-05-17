package co.edu.uniquindio.monedero.dominio.dto.transaccion;

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
public class TransaccionDTO {
    private String id;
    private double monto;
    private LocalDateTime fecha;
    private TipoTransaccion tipoTransaccion;
}
