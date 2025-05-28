package co.edu.uniquindio.monedero.infraestructura.controlador.monedero;

import co.edu.uniquindio.monedero.dominio.modelo.*;
import co.edu.uniquindio.monedero.dominio.servicios.monedero.ServicioMonedero;
import co.edu.uniquindio.monedero.infraestructura.grafo.Arista;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monederos")
@CrossOrigin(origins = "*")
public class ControladorMonedero {
    
    @Autowired
    private ServicioMonedero servicioMonedero;
    
    @PostMapping("/crear")
    public ResponseEntity<?> crearMonedero(@RequestBody CrearMonederoRequest request) {
        try {
            Monedero monedero = servicioMonedero.crearMonedero(
                request.getCedulaCliente(), 
                request.getNombre(), 
                request.getTipo()
            );
            return ResponseEntity.ok(monedero);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/transferir")
    public ResponseEntity<?> transferirEntreMonederos(@RequestBody TransferirEntreMonederosRequest request) {
        try {
            TransaccionEntreMonederos transaccion = servicioMonedero.transferirEntreMonederos(
                request.getCedulaCliente(),
                request.getIdMonederoOrigen(),
                request.getIdMonederoDestino(),
                request.getMonto(),
                request.getConcepto()
            );
            return ResponseEntity.ok(transaccion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/cliente/{cedulaCliente}")
    public ResponseEntity<?> obtenerMonederosPorCliente(@PathVariable String cedulaCliente) {
        try {
            List<Monedero> monederos = servicioMonedero.obtenerMonederosPorCliente(cedulaCliente);
            return ResponseEntity.ok(monederos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/cliente/{cedulaCliente}/monedero/{idMonedero}")
    public ResponseEntity<?> buscarMonederoPorId(
            @PathVariable String cedulaCliente, 
            @PathVariable String idMonedero) {
        try {
            Monedero monedero = servicioMonedero.buscarMonederoPorId(cedulaCliente, idMonedero);
            if (monedero != null) {
                return ResponseEntity.ok(monedero);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/cliente/{cedulaCliente}/camino-optimo")
    public ResponseEntity<?> obtenerCaminoOptimo(
            @PathVariable String cedulaCliente,
            @RequestParam String idOrigen,
            @RequestParam String idDestino) {
        try {
            List<String> camino = servicioMonedero.obtenerCaminoOptimo(cedulaCliente, idOrigen, idDestino);
            return ResponseEntity.ok(Map.of("camino", camino));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/establecer-relacion")
    public ResponseEntity<?> establecerRelacionMonederos(@RequestBody EstablecerRelacionRequest request) {
        try {
            servicioMonedero.establecerRelacionMonederos(
                request.getCedulaCliente(),
                request.getIdOrigen(),
                request.getIdDestino(),
                request.getComision()
            );
            return ResponseEntity.ok(Map.of("mensaje", "Relación establecida correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/cliente/{cedulaCliente}/monedero/{idMonedero}/relaciones")
    public ResponseEntity<?> obtenerRelacionesMonedero(
            @PathVariable String cedulaCliente,
            @PathVariable String idMonedero) {
        try {
            List<Arista<String>> relaciones = servicioMonedero.obtenerRelacionesMonedero(cedulaCliente, idMonedero);
            return ResponseEntity.ok(relaciones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/cliente/{cedulaCliente}/monedero/{idMonedero}")
    public ResponseEntity<?> eliminarMonedero(
            @PathVariable String cedulaCliente,
            @PathVariable String idMonedero) {
        try {
            servicioMonedero.eliminarMonedero(cedulaCliente, idMonedero);
            return ResponseEntity.ok(Map.of("mensaje", "Monedero eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/cliente/{cedulaCliente}/saldo-total")
    public ResponseEntity<?> calcularSaldoTotalMonederos(@PathVariable String cedulaCliente) {
        try {
            double saldoTotal = servicioMonedero.calcularSaldoTotalMonederos(cedulaCliente);
            return ResponseEntity.ok(Map.of("saldoTotal", saldoTotal));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/tipos")
    public ResponseEntity<?> obtenerTiposMonedero() {
        return ResponseEntity.ok(TipoMonedero.values());
    }
    
    @PostMapping("/transferir-de-cuenta")
    public ResponseEntity<?> transferirDeCuentaAMonedero(@RequestBody Map<String, Object> request) {
        try {
            String cedulaCliente = (String) request.get("cedulaCliente");
            String idMonedero = (String) request.get("idMonedero");
            double monto = Double.parseDouble(request.get("monto").toString());
            
            servicioMonedero.transferirDeCuentaAMonedero(cedulaCliente, idMonedero, monto);
            return ResponseEntity.ok(Map.of("mensaje", "Transferencia realizada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 