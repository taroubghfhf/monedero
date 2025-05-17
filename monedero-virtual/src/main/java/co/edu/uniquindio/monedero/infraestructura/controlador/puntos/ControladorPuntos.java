package co.edu.uniquindio.monedero.infraestructura.controlador.puntos;

import co.edu.uniquindio.monedero.dominio.modelo.RangoCliente;
import co.edu.uniquindio.monedero.dominio.modelo.TipoBeneficio;
import co.edu.uniquindio.monedero.dominio.servicios.puntos.GestorPuntosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/puntos")
public class ControladorPuntos {
    private final GestorPuntosService gestorPuntosService;

    public ControladorPuntos(GestorPuntosService gestorPuntosService) {
        this.gestorPuntosService = gestorPuntosService;
    }

    @GetMapping("/{cedulaCliente}")
    public ResponseEntity<Map<String, Object>> consultarPuntos(
            @PathVariable("cedulaCliente") String cedulaCliente) {
        try {
            int puntosAcumulados = gestorPuntosService.consultarPuntos(cedulaCliente);
            RangoCliente rango = gestorPuntosService.consultarRango(cedulaCliente);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("cedulaCliente", cedulaCliente);
            respuesta.put("puntosAcumulados", puntosAcumulados);
            respuesta.put("rango", rango.name());
            respuesta.put("puntosParaSiguienteRango", 
                calcularPuntosParaSiguienteRango(rango, puntosAcumulados));

            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private int calcularPuntosParaSiguienteRango(RangoCliente rangoActual, int puntosActuales) {
        return switch (rangoActual) {
            case BRONCE -> RangoCliente.PLATA.getPuntosMinimos() - puntosActuales;
            case PLATA -> RangoCliente.ORO.getPuntosMinimos() - puntosActuales;
            case ORO -> RangoCliente.PLATINO.getPuntosMinimos() - puntosActuales;
            case PLATINO -> 0;
        };
    }

    @PostMapping("/canjear/{cedulaCliente}")
    public ResponseEntity<Map<String, Object>> canjearPuntos(
            @PathVariable("cedulaCliente") String cedulaCliente,
            @RequestParam("puntos") int puntos) {
        try {
            boolean canjeado = gestorPuntosService.canjearPuntos(cedulaCliente, puntos);
            TipoBeneficio beneficio = TipoBeneficio.obtenerPorPuntos(puntos);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("exito", canjeado);
            respuesta.put("mensaje", canjeado ? 
                "Beneficio aplicado: " + beneficio.getDescripcion() :
                "No se pudo realizar el canje de puntos");
            respuesta.put("puntosRestantes", gestorPuntosService.consultarPuntos(cedulaCliente));
            
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/beneficios-disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerBeneficiosDisponibles() {
        List<Map<String, Object>> beneficios = Arrays.stream(TipoBeneficio.values())
            .map(b -> {
                Map<String, Object> beneficio = new HashMap<>();
                beneficio.put("tipo", b.name());
                beneficio.put("puntosRequeridos", b.getPuntosRequeridos());
                beneficio.put("descripcion", b.getDescripcion());
                return beneficio;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(beneficios);
    }
} 