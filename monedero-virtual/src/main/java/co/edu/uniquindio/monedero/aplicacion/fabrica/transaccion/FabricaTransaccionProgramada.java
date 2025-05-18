package co.edu.uniquindio.monedero.aplicacion.fabrica.transaccion;

import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionProgramadaComando;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import org.springframework.stereotype.Component;

@Component
public class FabricaTransaccionProgramada {
    
    public TransaccionProgramada crear(TransaccionProgramadaComando comando) {
        return new TransaccionProgramada(
            comando.getClienteOrigen(),
            comando.getClienteDestino(),
            comando.getMonto(),
            comando.getTipoTransaccion(),
            comando.getFechaEjecucion(),
            comando.getPeriodicidad()
        );
    }
} 