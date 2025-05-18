package co.edu.uniquindio.monedero.infraestructura.controlador.programada;

import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionProgramadaComando;
import co.edu.uniquindio.monedero.aplicacion.manejador.transaccion.*;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones-programadas")
@RequiredArgsConstructor
public class TransaccionProgramadaControlador {
    private final ManejadorProgramarTransaccion manejadorProgramar;
    private final ManejadorCancelarTransaccionProgramada manejadorCancelar;
    private final ManejadorListarTransaccionesProgramadas manejadorListar;

    @PostMapping
    public ResponseEntity<String> programarTransaccion(@RequestBody TransaccionProgramadaComando comando) {
        try {
            String id = manejadorProgramar.ejecutar(comando);
            return ResponseEntity.ok("{\"id\":\"" + id + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelarTransaccion(@PathVariable String id) {
        try {
            manejadorCancelar.ejecutar(id);
            return ResponseEntity.ok("{\"success\":true}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }


    @GetMapping("/cliente/{numeroCliente}")
    public ResponseEntity<List<TransaccionProgramada>> listarPorCliente(@PathVariable String numeroCliente) {
        try {
            List<TransaccionProgramada> transacciones = manejadorListar.ejecutar(numeroCliente);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 