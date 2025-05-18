package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionProgramadaComando;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import co.edu.uniquindio.monedero.dominio.servicios.transaccion.GestorTransaccionesProgramadasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManejadorTransaccionProgramada {
    private final GestorTransaccionesProgramadasService gestorService;

    public String ejecutar(TransaccionProgramadaComando comando) {
        TransaccionProgramada transaccion = new TransaccionProgramada(
            comando.getClienteOrigen(),
            comando.getClienteDestino(),
            comando.getMonto(),
            comando.getTipoTransaccion(),
            comando.getFechaEjecucion(),
            comando.getPeriodicidad()
        );

        gestorService.programarTransaccion(transaccion);
        return transaccion.getId();
    }

    public void cancelar(String id) {
        gestorService.cancelarTransaccionPorId(id);
    }
} 