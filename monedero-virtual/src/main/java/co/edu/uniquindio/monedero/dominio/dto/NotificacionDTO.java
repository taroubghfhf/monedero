package co.edu.uniquindio.monedero.dominio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para representar una notificación en el sistema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    
    private String id;
    private String destinatario;
    private String asunto;
    private String template;
    private Map<String, Object> variables;
    private TipoNotificacion tipo;
    private PrioridadNotificacion prioridad;
    private EstadoNotificacion estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
    private int intentosEnvio;
    private String mensajeError;
    
    public enum TipoNotificacion {
        ALERTA_SALDO_BAJO,
        RECORDATORIO_TRANSACCION_PROGRAMADA,
        BIENVENIDA,
        TRANSACCION_EXITOSA,
        TRANSACCION_FALLIDA,
        BLOQUEO_CUENTA,
        ACTIVACION_BENEFICIO
    }
    
    public enum PrioridadNotificacion {
        BAJA(1),
        MEDIA(2),
        ALTA(3),
        CRITICA(4);
        
        private final int nivel;
        
        PrioridadNotificacion(int nivel) {
            this.nivel = nivel;
        }
        
        public int getNivel() {
            return nivel;
        }
    }
    
    public enum EstadoNotificacion {
        PENDIENTE,
        ENVIADA,
        FALLIDA,
        REINTENTANDO
    }
} 