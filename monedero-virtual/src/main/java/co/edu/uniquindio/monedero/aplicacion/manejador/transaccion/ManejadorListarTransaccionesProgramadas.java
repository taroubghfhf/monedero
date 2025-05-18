package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import co.edu.uniquindio.monedero.dominio.servicios.transaccion.GestorTransaccionesProgramadasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ManejadorListarTransaccionesProgramadas {
    private final GestorTransaccionesProgramadasService gestorService;

    public List<TransaccionProgramada> ejecutar(String numeroCliente) {
        return gestorService.listarTransaccionesPorCliente(numeroCliente);
    }
} 