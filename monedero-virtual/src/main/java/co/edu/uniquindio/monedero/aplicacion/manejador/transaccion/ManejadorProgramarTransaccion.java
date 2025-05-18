package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionProgramadaComando;
import co.edu.uniquindio.monedero.aplicacion.fabrica.transaccion.FabricaTransaccionProgramada;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import co.edu.uniquindio.monedero.dominio.servicios.transaccion.GestorTransaccionesProgramadasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManejadorProgramarTransaccion {
    private final GestorTransaccionesProgramadasService gestorService;
    private final FabricaTransaccionProgramada fabricaTransaccion;

    public String ejecutar(TransaccionProgramadaComando comando) {
        TransaccionProgramada transaccion = fabricaTransaccion.crear(comando);
        gestorService.programarTransaccion(transaccion);
        return transaccion.getId();
    }
} 