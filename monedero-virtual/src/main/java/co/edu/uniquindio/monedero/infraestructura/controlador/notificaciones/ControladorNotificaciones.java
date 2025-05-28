package co.edu.uniquindio.monedero.infraestructura.controlador.notificaciones;

import co.edu.uniquindio.monedero.infraestructura.notificaciones.GestorNotificaciones;
import co.edu.uniquindio.monedero.infraestructura.notificaciones.ServicioAlertas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestionar y monitorear el sistema de notificaciones
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
public class ControladorNotificaciones {
    
    private final GestorNotificaciones gestorNotificaciones;
    private final ServicioAlertas servicioAlertas;
    
    /**
     * Obtiene estadísticas generales del sistema de notificaciones
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            GestorNotificaciones.EstadisticasNotificaciones statsGestor = 
                gestorNotificaciones.obtenerEstadisticas();
            
            ServicioAlertas.EstadisticasAlertas statsAlertas = 
                servicioAlertas.obtenerEstadisticas();
            
            Map<String, Object> estadisticas = Map.of(
                "gestor", Map.of(
                    "notificacionesPendientes", statsGestor.getNotificacionesPendientes(),
                    "capacidadTotal", statsGestor.getCapacidadTotal(),
                    "porcentajeUso", statsGestor.getPorcentajeUso(),
                    "intervaloProcesamiento", statsGestor.getIntervaloProcesamiento()
                ),
                "alertas", Map.of(
                    "alertasEnCache", statsAlertas.getAlertasEnCache(),
                    "saldoBajoHabilitado", statsAlertas.isSaldoBajoHabilitado(),
                    "transaccionesProgramadasHabilitado", statsAlertas.isTransaccionesProgramadasHabilitado(),
                    "umbralSaldoBajo", statsAlertas.getUmbralSaldoBajo()
                ),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de notificaciones: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Limpia todas las notificaciones pendientes (solo para desarrollo/testing)
     */
    @DeleteMapping("/limpiar")
    public ResponseEntity<Map<String, String>> limpiarNotificaciones() {
        try {
            gestorNotificaciones.limpiarNotificacionesPendientes();
            
            Map<String, String> respuesta = Map.of(
                "mensaje", "Notificaciones pendientes limpiadas exitosamente",
                "timestamp", String.valueOf(System.currentTimeMillis())
            );
            
            log.info("Notificaciones limpiadas manualmente desde API");
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error al limpiar notificaciones: {}", e.getMessage(), e);
            Map<String, String> error = Map.of(
                "error", "Error interno al limpiar notificaciones",
                "timestamp", String.valueOf(System.currentTimeMillis())
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Endpoint de salud para verificar que el sistema de notificaciones esté funcionando
     */
    @GetMapping("/salud")
    public ResponseEntity<Map<String, Object>> verificarSalud() {
        try {
            GestorNotificaciones.EstadisticasNotificaciones stats = 
                gestorNotificaciones.obtenerEstadisticas();
            
            boolean saludable = stats.getCapacidadTotal() > 0;
            
            Map<String, Object> salud = Map.of(
                "estado", saludable ? "SALUDABLE" : "ERROR",
                "descripcion", saludable ? "Sistema de notificaciones funcionando correctamente" : "Sistema de notificaciones no disponible",
                "detalles", Map.of(
                    "capacidadDisponible", stats.getCapacidadTotal() - stats.getNotificacionesPendientes(),
                    "sistemaActivo", true
                ),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(salud);
            
        } catch (Exception e) {
            log.error("Error al verificar salud del sistema de notificaciones: {}", e.getMessage(), e);
            
            Map<String, Object> error = Map.of(
                "estado", "ERROR",
                "descripcion", "Error al verificar el estado del sistema",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
} 