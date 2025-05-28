package co.edu.uniquindio.monedero.infraestructura.notificaciones;

import co.edu.uniquindio.monedero.dominio.dto.NotificacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Gestor principal de notificaciones que coordina el procesamiento
 * usando lista circular y envío de emails
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GestorNotificaciones {
    
    private final ListaCircularNotificaciones listaCircular;
    private final ServicioEmail servicioEmail;
    
    @Value("${notificaciones.lista-circular.intervalo-procesamiento:30}")
    private int intervaloProcesamiento;
    
    private static final int MAX_INTENTOS_ENVIO = 3;
    private static final long DELAY_REINTENTO_MS = 5000; // 5 segundos
    
    /**
     * Agenda una notificación para ser enviada
     */
    public boolean agendarNotificacion(NotificacionDTO notificacion) {
        try {
            log.info("Agendando notificación: {} para {}", 
                    notificacion.getId(), notificacion.getDestinatario());
            
            // Validar email antes de agregar a la lista
            if (!servicioEmail.validarEmail(notificacion.getDestinatario())) {
                log.warn("Email inválido, no se puede agendar notificación: {}", 
                        notificacion.getDestinatario());
                return false;
            }
            
            boolean agregada = listaCircular.agregar(notificacion);
            
            if (agregada) {
                log.info("Notificación {} agregada exitosamente a la lista circular", 
                        notificacion.getId());
            } else {
                log.error("No se pudo agregar la notificación {} a la lista circular", 
                         notificacion.getId());
            }
            
            return agregada;
            
        } catch (Exception e) {
            log.error("Error al agendar notificación {}: {}", 
                     notificacion.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Procesa las notificaciones pendientes de forma asíncrona
     * Se ejecuta cada intervalo configurado
     */
    @Scheduled(fixedDelayString = "${notificaciones.lista-circular.intervalo-procesamiento:30}000")
    @Async
    public void procesarNotificacionesPendientes() {
        try {
            log.debug("Iniciando procesamiento de notificaciones pendientes");
            
            // Procesar notificaciones críticas primero
            procesarNotificacionesPorPrioridad(NotificacionDTO.PrioridadNotificacion.CRITICA);
            
            // Luego las de alta prioridad
            procesarNotificacionesPorPrioridad(NotificacionDTO.PrioridadNotificacion.ALTA);
            
            // Procesar una notificación normal por ciclo para balancear carga
            procesarSiguienteNotificacion();
            
            // Mostrar estadísticas cada 10 ciclos
            mostrarEstadisticasPeriodicas();
            
        } catch (Exception e) {
            log.error("Error durante el procesamiento de notificaciones: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Procesa notificaciones por prioridad específica
     */
    private void procesarNotificacionesPorPrioridad(NotificacionDTO.PrioridadNotificacion prioridad) {
        List<NotificacionDTO> notificacionesPrioritarias = 
            listaCircular.obtenerNotificacionesPorPrioridad(prioridad);
        
        for (NotificacionDTO notificacion : notificacionesPrioritarias) {
            procesarNotificacion(notificacion);
        }
    }
    
    /**
     * Procesa la siguiente notificación en la lista circular
     */
    private void procesarSiguienteNotificacion() {
        NotificacionDTO notificacion = listaCircular.obtenerSiguiente();
        
        if (notificacion != null) {
            procesarNotificacion(notificacion);
        }
    }
    
    /**
     * Procesa una notificación individual
     */
    private void procesarNotificacion(NotificacionDTO notificacion) {
        try {
            log.debug("Procesando notificación: {}", notificacion.getId());
            
            // Verificar si ya se envió o excedió intentos
            if (notificacion.getEstado() == NotificacionDTO.EstadoNotificacion.ENVIADA ||
                notificacion.getIntentosEnvio() >= MAX_INTENTOS_ENVIO) {
                
                listaCircular.marcarComoProcesada(notificacion.getId());
                log.debug("Notificación {} removida de la lista (enviada o max intentos)", 
                         notificacion.getId());
                return;
            }
            
            // Actualizar estado e intentos
            notificacion.setEstado(NotificacionDTO.EstadoNotificacion.REINTENTANDO);
            notificacion.setIntentosEnvio(notificacion.getIntentosEnvio() + 1);
            
            // Intentar enviar
            boolean enviada = servicioEmail.enviarNotificacion(notificacion);
            
            if (enviada) {
                notificacion.setEstado(NotificacionDTO.EstadoNotificacion.ENVIADA);
                notificacion.setFechaEnvio(LocalDateTime.now());
                listaCircular.marcarComoProcesada(notificacion.getId());
                
                log.info("Notificación {} enviada exitosamente", notificacion.getId());
                
            } else {
                notificacion.setEstado(NotificacionDTO.EstadoNotificacion.FALLIDA);
                notificacion.setMensajeError("Error en el envío - Intento " + notificacion.getIntentosEnvio());
                
                if (notificacion.getIntentosEnvio() >= MAX_INTENTOS_ENVIO) {
                    listaCircular.marcarComoProcesada(notificacion.getId());
                    log.error("Notificación {} falló después de {} intentos", 
                             notificacion.getId(), MAX_INTENTOS_ENVIO);
                } else {
                    log.warn("Notificación {} falló, se reintentará. Intento {}/{}", 
                            notificacion.getId(), notificacion.getIntentosEnvio(), MAX_INTENTOS_ENVIO);
                }
            }
            
        } catch (Exception e) {
            log.error("Error al procesar notificación {}: {}", 
                     notificacion.getId(), e.getMessage(), e);
            
            notificacion.setEstado(NotificacionDTO.EstadoNotificacion.FALLIDA);
            notificacion.setMensajeError("Error inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Envía una notificación inmediatamente (sin usar la lista circular)
     */
    @Async
    public CompletableFuture<Boolean> enviarNotificacionInmediata(NotificacionDTO notificacion) {
        try {
            log.info("Enviando notificación inmediata: {}", notificacion.getId());
            
            if (!servicioEmail.validarEmail(notificacion.getDestinatario())) {
                log.warn("Email inválido para notificación inmediata: {}", 
                        notificacion.getDestinatario());
                return CompletableFuture.completedFuture(false);
            }
            
            boolean enviada = servicioEmail.enviarNotificacion(notificacion);
            
            if (enviada) {
                notificacion.setEstado(NotificacionDTO.EstadoNotificacion.ENVIADA);
                notificacion.setFechaEnvio(LocalDateTime.now());
                log.info("Notificación inmediata {} enviada exitosamente", notificacion.getId());
            } else {
                notificacion.setEstado(NotificacionDTO.EstadoNotificacion.FALLIDA);
                log.error("Error al enviar notificación inmediata {}", notificacion.getId());
            }
            
            return CompletableFuture.completedFuture(enviada);
            
        } catch (Exception e) {
            log.error("Error al enviar notificación inmediata {}: {}", 
                     notificacion.getId(), e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    /**
     * Obtiene estadísticas del sistema de notificaciones
     */
    public EstadisticasNotificaciones obtenerEstadisticas() {
        ListaCircularNotificaciones.EstadisticasListaCircular stats = 
            listaCircular.obtenerEstadisticas();
        
        return new EstadisticasNotificaciones(
            stats.getTamaño(),
            stats.getCapacidad(),
            stats.getPorcentajeUso(),
            intervaloProcesamiento
        );
    }
    
    /**
     * Muestra estadísticas periódicas en los logs
     */
    private static int contadorCiclos = 0;
    private void mostrarEstadisticasPeriodicas() {
        contadorCiclos++;
        if (contadorCiclos % 10 == 0) { // Cada 10 ciclos
            ListaCircularNotificaciones.EstadisticasListaCircular stats = 
                listaCircular.obtenerEstadisticas();
            log.info("Estadísticas de notificaciones: {}", stats);
        }
    }
    
    /**
     * Limpia todas las notificaciones pendientes
     */
    public void limpiarNotificacionesPendientes() {
        listaCircular.limpiar();
        log.info("Lista de notificaciones limpiada");
    }
    
    /**
     * Clase para estadísticas del gestor de notificaciones
     */
    public static class EstadisticasNotificaciones {
        private final int notificacionesPendientes;
        private final int capacidadTotal;
        private final double porcentajeUso;
        private final int intervaloProcesamiento;
        
        public EstadisticasNotificaciones(int pendientes, int capacidad, 
                                        double porcentaje, int intervalo) {
            this.notificacionesPendientes = pendientes;
            this.capacidadTotal = capacidad;
            this.porcentajeUso = porcentaje;
            this.intervaloProcesamiento = intervalo;
        }
        
        // Getters
        public int getNotificacionesPendientes() { return notificacionesPendientes; }
        public int getCapacidadTotal() { return capacidadTotal; }
        public double getPorcentajeUso() { return porcentajeUso; }
        public int getIntervaloProcesamiento() { return intervaloProcesamiento; }
        
        @Override
        public String toString() {
            return String.format("EstadisticasNotificaciones{pendientes=%d, capacidad=%d, " +
                               "uso=%.2f%%, intervalo=%ds}", 
                               notificacionesPendientes, capacidadTotal, porcentajeUso, intervaloProcesamiento);
        }
    }
} 