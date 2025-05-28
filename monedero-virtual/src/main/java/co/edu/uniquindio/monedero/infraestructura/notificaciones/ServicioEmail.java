package co.edu.uniquindio.monedero.infraestructura.notificaciones;

import co.edu.uniquindio.monedero.dominio.dto.NotificacionDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Servicio para envío de emails usando Gmail SMTP
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioEmail {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${notificaciones.templates.path:classpath:templates/email/}")
    private String templatesPath;
    
    /**
     * Envía una notificación por email
     */
    public boolean enviarNotificacion(NotificacionDTO notificacion) {
        try {
            log.info("Enviando notificación por email: {} a {}", 
                    notificacion.getId(), notificacion.getDestinatario());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Configurar destinatario y remitente
            helper.setTo(notificacion.getDestinatario());
            helper.setFrom(fromEmail);
            helper.setSubject(notificacion.getAsunto());
            
            // Procesar template con variables
            String contenidoHtml = procesarTemplate(notificacion.getTemplate(), notificacion.getVariables());
            helper.setText(contenidoHtml, true);
            
            // Enviar el email
            mailSender.send(message);
            
            log.info("Notificación enviada exitosamente: {}", notificacion.getId());
            return true;
            
        } catch (MessagingException e) {
            log.error("Error al enviar notificación {}: {}", notificacion.getId(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error inesperado al enviar notificación {}: {}", notificacion.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Procesa un template de Thymeleaf con las variables proporcionadas
     */
    private String procesarTemplate(String template, Map<String, Object> variables) {
        try {
            Context context = new Context();
            
            // Agregar variables del negocio
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            
            // Agregar variables globales
            context.setVariable("fecha", LocalDateTime.now());
            context.setVariable("año", LocalDateTime.now().getYear());
            context.setVariable("nombreApp", "Monedero Virtual Uniquindío");
            
            return templateEngine.process(template, context);
            
        } catch (Exception e) {
            log.error("Error al procesar template {}: {}", template, e.getMessage(), e);
            // Fallback a contenido simple si el template falla
            return generarContenidoFallback(variables);
        }
    }
    
    /**
     * Genera contenido HTML básico en caso de que falle el template
     */
    private String generarContenidoFallback(Map<String, Object> variables) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html><head><meta charset='UTF-8'></head><body>")
            .append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>")
            .append("<h2 style='color: #2c3e50;'>Monedero Virtual Uniquindío</h2>")
            .append("<p>Estimado cliente,</p>")
            .append("<p>Le informamos sobre una actividad importante en su cuenta:</p>");
        
        if (variables != null) {
            variables.forEach((key, value) -> {
                if (value != null) {
                    html.append("<p><strong>").append(key).append(":</strong> ").append(value).append("</p>");
                }
            });
        }
        
        html.append("<p>Gracias por confiar en nosotros.</p>")
            .append("<p><em>Equipo Monedero Virtual Uniquindío</em></p>")
            .append("</div></body></html>");
        
        return html.toString();
    }
    
    /**
     * Valida si una dirección de email es válida
     */
    public boolean validarEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.length() > 5 &&
               email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    /**
     * Crea una notificación de bienvenida para nuevos clientes
     */
    public NotificacionDTO crearNotificacionBienvenida(String emailCliente, String nombreCliente) {
        Map<String, Object> variables = Map.of(
            "nombreCliente", nombreCliente,
            "emailCliente", emailCliente
        );
        
        return NotificacionDTO.builder()
                .id(java.util.UUID.randomUUID().toString())
                .destinatario(emailCliente)
                .asunto("¡Bienvenido a Monedero Virtual Uniquindío!")
                .template("email/bienvenida")
                .variables(variables)
                .tipo(NotificacionDTO.TipoNotificacion.BIENVENIDA)
                .prioridad(NotificacionDTO.PrioridadNotificacion.MEDIA)
                .estado(NotificacionDTO.EstadoNotificacion.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .intentosEnvio(0)
                .build();
    }
    
    /**
     * Crea una notificación de alerta de saldo bajo
     */
    public NotificacionDTO crearNotificacionSaldoBajo(String emailCliente, String nombreCliente, 
                                                    double saldoActual, double umbral) {
        Map<String, Object> variables = Map.of(
            "nombreCliente", nombreCliente,
            "saldoActual", String.format("$%,.2f", saldoActual),
            "umbral", String.format("$%,.2f", umbral),
            "fechaAlerta", LocalDateTime.now().toLocalDate()
        );
        
        return NotificacionDTO.builder()
                .id(java.util.UUID.randomUUID().toString())
                .destinatario(emailCliente)
                .asunto("⚠️ Alerta: Saldo Bajo en su Monedero Virtual")
                .template("email/alerta-saldo-bajo")
                .variables(variables)
                .tipo(NotificacionDTO.TipoNotificacion.ALERTA_SALDO_BAJO)
                .prioridad(NotificacionDTO.PrioridadNotificacion.ALTA)
                .estado(NotificacionDTO.EstadoNotificacion.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .intentosEnvio(0)
                .build();
    }
    
    /**
     * Crea una notificación de recordatorio de transacción programada
     */
    public NotificacionDTO crearNotificacionRecordatorioTransaccion(String emailCliente, String nombreCliente,
                                                                  String tipoTransaccion, double monto, 
                                                                  LocalDateTime fechaEjecucion) {
        Map<String, Object> variables = Map.of(
            "nombreCliente", nombreCliente,
            "tipoTransaccion", tipoTransaccion,
            "monto", String.format("$%,.2f", monto),
            "fechaEjecucion", fechaEjecucion.toLocalDate(),
            "horaEjecucion", fechaEjecucion.toLocalTime()
        );
        
        return NotificacionDTO.builder()
                .id(java.util.UUID.randomUUID().toString())
                .destinatario(emailCliente)
                .asunto("📅 Recordatorio: Transacción Programada")
                .template("email/recordatorio-transaccion")
                .variables(variables)
                .tipo(NotificacionDTO.TipoNotificacion.RECORDATORIO_TRANSACCION_PROGRAMADA)
                .prioridad(NotificacionDTO.PrioridadNotificacion.MEDIA)
                .estado(NotificacionDTO.EstadoNotificacion.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .intentosEnvio(0)
                .build();
    }
} 