package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.dominio.servicios.transaccion.GestorTransaccionesProgramadasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManejadorCancelarTransaccionProgramada {
    private final GestorTransaccionesProgramadasService gestorService;

    public void ejecutar(String id) {
        gestorService.cancelarTransaccionPorId(id);
    }
} 