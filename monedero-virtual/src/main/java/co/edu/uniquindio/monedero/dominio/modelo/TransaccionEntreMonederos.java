package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntreMonederos extends Transaccion {
    private String monederoOrigen;
    private String monederoDestino;
    private String concepto;
    
    public TransaccionEntreMonederos(String codigo, double monto, LocalDateTime fecha, 
                                   String monederoOrigen, String monederoDestino, String concepto) {
        super(codigo, monto, fecha, TipoTransaccion.TRANSFERENCIA_ENTRE_MONEDEROS);
        this.monederoOrigen = monederoOrigen;
        this.monederoDestino = monederoDestino;
        this.concepto = concepto;
    }
} 