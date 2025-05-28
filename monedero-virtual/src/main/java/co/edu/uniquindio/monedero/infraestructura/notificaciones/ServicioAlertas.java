package co.edu.uniquindio.monedero.infraestructura.notificaciones;

import co.edu.uniquindio.monedero.dominio.dto.NotificacionDTO;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import co.edu.uniquindio.monedero.dominio.puerto.transaccion.TransaccionProgramadaRepositorio;
import co.edu.uniquindio.monedero.infraestructura.persistencia.Lista;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioAlertas {
    
    private final GestorNotificaciones gestorNotificaciones;
    private final ServicioEmail servicioEmail;
    private final TransaccionProgramadaRepositorio transaccionProgramadaRepositorio;

    @Value("${notificaciones.alertas.saldo-bajo.umbral:10000}")
    private double umbralSaldoBajo;
    
    @Value("${notificaciones.alertas.saldo-bajo.habilitado:true}")
    private boolean alertaSaldoBajoHabilitada;
    
    @Value("${notificaciones.alertas.transacciones-programadas.recordatorio-anticipado:86400}")
    private int recordatorioAnticipadoSegundos;
    
    @Value("${notificaciones.alertas.transacciones-programadas.habilitado:true}")
    private boolean alertaTransaccionesProgramadasHabilitada;

    private final Set<String> alertasRecientesCache = ConcurrentHashMap.newKeySet();
    
    /**
     * Revisa periódicamente las cuentas para detectar saldos bajos
     */
    @Scheduled(fixedDelayString = "${notificaciones.alertas.saldo-bajo.frecuencia-revision:3600}000")
    public void revisarSaldosBajos() {
        if (!alertaSaldoBajoHabilitada) {
            log.debug("Alertas de saldo bajo deshabilitadas");
            return;
        }
        
        try {
            log.debug("Iniciando revisión de saldos bajos. Umbral: ${}", umbralSaldoBajo);
            
            List<Cliente> clientesConSaldoBajo = obtenerCuentasConSaldoBajo();
            
            for (Cliente cliente : clientesConSaldoBajo) {
                Cuenta cuenta = cliente.getCuenta();
                if (cuenta != null) {
                    procesarAlertaSaldoBajo(cuenta);
                }
            }
            
            log.info("Revisión de saldos bajos completada. {} clientes procesados", 
                    clientesConSaldoBajo.size());
            
        } catch (Exception e) {
            log.error("Error durante la revisión de saldos bajos: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Revisa periódicamente las transacciones programadas para enviar recordatorios
     * Se ejecuta cada 2 horas por defecto
     */
    @Scheduled(fixedDelayString = "${notificaciones.alertas.transacciones-programadas.frecuencia-revision:7200}000")
    public void revisarTransaccionesProgramadas() {
        if (!alertaTransaccionesProgramadasHabilitada) {
            log.debug("Alertas de transacciones programadas deshabilitadas");
            return;
        }
        
        try {
            log.debug("Iniciando revisión de transacciones programadas");
            
            List<TransaccionProgramada> transaccionesPorNotificar = 
                obtenerTransaccionesPorNotificar();
            
            for (TransaccionProgramada transaccion : transaccionesPorNotificar) {
                procesarRecordatorioTransaccion(transaccion);
            }
            
            log.info("Revisión de transacciones programadas completada. {} transacciones procesadas", 
                    transaccionesPorNotificar.size());
            
        } catch (Exception e) {
            log.error("Error durante la revisión de transacciones programadas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Procesa la alerta de saldo bajo para una cuenta específica
     */
    private void procesarAlertaSaldoBajo(Cuenta cuenta) {
        try {
            String claveCache = "saldo-bajo-" + cuenta.getNumeroCuenta();

            if (alertasRecientesCache.contains(claveCache)) {
                log.debug("Alerta de saldo bajo ya enviada recientemente para cuenta: {}", cuenta.getNumeroCuenta());
                return;
            }
            
            Cliente cliente = obtenerClientePorCuenta(cuenta);
            if (cliente == null || cliente.getCorreo() == null) {
                log.warn("No se puede enviar alerta de saldo bajo - Cliente o email no encontrado para cuenta: {}", 
                        cuenta.getNumeroCuenta());
                return;
            }

            NotificacionDTO notificacion = servicioEmail.crearNotificacionSaldoBajo(
                cliente.getCorreo(),
                cliente.getNombre(),
                cuenta.getSaldoCuenta(),
                umbralSaldoBajo
            );
            
            boolean agendada = gestorNotificaciones.agendarNotificacion(notificacion);
            
            if (agendada) {
                alertasRecientesCache.add(claveCache);
                log.info("Alerta de saldo bajo agendada para cuenta: {} - Saldo: ${}", 
                        cuenta.getNumeroCuenta(), cuenta.getSaldoCuenta());
            } else {
                log.error("No se pudo agendar alerta de saldo bajo para cuenta: {}", cuenta.getNumeroCuenta());
            }
            
        } catch (Exception e) {
            log.error("Error al procesar alerta de saldo bajo para cuenta {}: {}", 
                     cuenta.getNumeroCuenta(), e.getMessage(), e);
        }
    }
    
    /**
     * Procesa el recordatorio para una transacción programada
     */
    private void procesarRecordatorioTransaccion(TransaccionProgramada transaccion) {
        try {
            String claveCache = "recordatorio-" + transaccion.getId();
            
            // Verificar si ya se envió un recordatorio
            if (alertasRecientesCache.contains(claveCache)) {
                log.debug("Recordatorio ya enviado para transacción: {}", transaccion.getId());
                return;
            }
            
            Cliente cliente = obtenerClientePorTransaccion(transaccion);
            if (cliente == null || cliente.getCorreo() == null) {
                log.warn("No se puede enviar recordatorio - Cliente o email no encontrado para transacción: {}", 
                        transaccion.getId());
                return;
            }
            
            // Crear y agendar la notificación
            NotificacionDTO notificacion = servicioEmail.crearNotificacionRecordatorioTransaccion(
                cliente.getCorreo(),
                cliente.getNombre(),
                transaccion.getTipoTransaccion().toString(),
                transaccion.getMonto(),
                transaccion.getFechaEjecucion()
            );
            
            boolean agendada = gestorNotificaciones.agendarNotificacion(notificacion);
            
            if (agendada) {
                // Agregar al cache para evitar duplicados
                alertasRecientesCache.add(claveCache);
                
                log.info("Recordatorio de transacción agendado para: {} - Transacción: {} - Fecha: {}", 
                        cliente.getCorreo(), transaccion.getId(), transaccion.getFechaEjecucion());
            } else {
                log.error("No se pudo agendar recordatorio para transacción: {}", transaccion.getId());
            }
            
        } catch (Exception e) {
            log.error("Error al procesar recordatorio para transacción {}: {}", 
                     transaccion.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Envía una notificación de bienvenida inmediata para nuevos clientes
     */
    public void enviarNotificacionBienvenida(Cliente cliente) {
        try {
            if (cliente.getCorreo() == null || cliente.getCorreo().isEmpty()) {
                log.warn("No se puede enviar notificación de bienvenida - Email no disponible para cliente: {}", 
                        cliente.getCedula());
                return;
            }
            
            NotificacionDTO notificacion = servicioEmail.crearNotificacionBienvenida(
                cliente.getCorreo(),
                cliente.getNombre()
            );

            gestorNotificaciones.enviarNotificacionInmediata(notificacion)
                .thenAccept(enviada -> {
                    if (enviada) {
                        log.info("Notificación de bienvenida enviada a: {}", cliente.getCorreo());
                    } else {
                        log.error("Error al enviar notificación de bienvenida a: {}", cliente.getCorreo());
                    }
                });
            
        } catch (Exception e) {
            log.error("Error al enviar notificación de bienvenida para cliente {}: {}", 
                     cliente.getCedula(), e.getMessage(), e);
        }
    }

    /**
     * Limpia el cache de alertas recientes (se ejecuta diariamente)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Todos los días a las 2 AM
    public void limpiarCacheAlertas() {
        int tamañoAnterior = alertasRecientesCache.size();
        alertasRecientesCache.clear();
        log.info("Cache de alertas limpiado. Elementos removidos: {}", tamañoAnterior);
    }
    
    /**
     * Obtiene estadísticas del servicio de alertas
     */
    public EstadisticasAlertas obtenerEstadisticas() {
        return new EstadisticasAlertas(
            alertasRecientesCache.size(),
            alertaSaldoBajoHabilitada,
            alertaTransaccionesProgramadasHabilitada,
            umbralSaldoBajo
        );
    }


    private List<Cliente> obtenerCuentasConSaldoBajo() {
        try {
            log.debug("Buscando clientes con cuentas con saldo mayor a 0 y menor a 2000");
            
            // Obtener la lista simple de todos los clientes
            Lista listaClientes = Lista.obtenerLista();
            List<Cliente> todosLosClientes = listaClientes.obtenerTodosLosClientes();
            
            List<Cliente> clientesConSaldoBajo = new ArrayList<>();
            
            // Recorrer todos los clientes y verificar sus cuentas
            for (Cliente cliente : todosLosClientes) {
                Cuenta cuenta = cliente.getCuenta();
                
                // Verificar que el cliente tenga cuenta y que cumpla con los criterios
                if (cuenta != null) {
                    double saldo = cuenta.getSaldoCuenta();
                    
                    // Validar que el saldo esté entre 0 y 2000 (exclusivo de 0, inclusivo de 2000)
                    if (saldo > 0 && saldo < 2000) {
                        clientesConSaldoBajo.add(cliente);
                        log.debug("Cliente encontrado con saldo bajo: {} - Cuenta: {} - Saldo: ${}", 
                                cliente.getNombre(), cuenta.getNumeroCuenta(), saldo);
                    }
                }
            }
            
            log.info("Se encontraron {} clientes con saldo bajo (entre $0 y $2000)", 
                    clientesConSaldoBajo.size());
            
            return clientesConSaldoBajo;
            
        } catch (Exception e) {
            log.error("Error al obtener clientes con saldo bajo: {}", e.getMessage(), e);
            return new ArrayList<>(); // Retornar lista vacía en caso de error
        }
    }
    
    private List<TransaccionProgramada> obtenerTransaccionesPorNotificar() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime fechaLimite = ahora.plusSeconds(recordatorioAnticipadoSegundos);
            
            log.debug("Buscando transacciones programadas entre {} y {}", ahora, fechaLimite);
            
            // Usar el nuevo método del repositorio para obtener transacciones en el rango de fechas
            List<TransaccionProgramada> transaccionesPorNotificar = 
                transaccionProgramadaRepositorio.buscarPorFechaEjecucionEntre(ahora, fechaLimite);
            
            log.info("Se encontraron {} transacciones programadas para notificar", 
                    transaccionesPorNotificar.size());
            
            // Log de transacciones encontradas para debugging
            for (TransaccionProgramada transaccion : transaccionesPorNotificar) {
                log.debug("Transacción encontrada para notificar: {} - Cliente: {} - Fecha: {}", 
                        transaccion.getId(), transaccion.getClienteOrigen(), transaccion.getFechaEjecucion());
            }
            
            return transaccionesPorNotificar;
            
        } catch (Exception e) {
            log.error("Error al obtener transacciones por notificar: {}", e.getMessage(), e);
            return new ArrayList<>(); // Retornar lista vacía en caso de error
        }
    }
    
    private Cliente obtenerClientePorCuenta(Cuenta cuenta) {
        try {
            Lista listaClientes = Lista.obtenerLista();
            List<Cliente> todosLosClientes = listaClientes.obtenerTodosLosClientes();

            for (Cliente cliente : todosLosClientes) {
                if (cliente.getCuenta() != null && 
                    cliente.getCuenta().getNumeroCuenta().equals(cuenta.getNumeroCuenta())) {
                    return cliente;
                }
            }
            
            log.debug("No se encontró cliente para la cuenta: {}", cuenta.getNumeroCuenta());
            return null;
            
        } catch (Exception e) {
            log.error("Error al buscar cliente para cuenta {}: {}", cuenta.getNumeroCuenta(), e.getMessage(), e);
            return null;
        }
    }
    
    private Cliente obtenerClientePorTransaccion(TransaccionProgramada transaccion) {
        try {
            // Obtener el cliente origen de la transacción usando su cédula
            String cedulaClienteOrigen = transaccion.getClienteOrigen();
            
            if (cedulaClienteOrigen == null || cedulaClienteOrigen.trim().isEmpty()) {
                log.warn("No se puede obtener cliente - Cédula de cliente origen vacía para transacción: {}", 
                        transaccion.getId());
                return null;
            }
            
            Lista listaClientes = Lista.obtenerLista();
            Cliente cliente = listaClientes.buscarCliente(cedulaClienteOrigen);
            
            if (cliente == null) {
                log.warn("No se encontró cliente con cédula: {} para transacción: {}", 
                        cedulaClienteOrigen, transaccion.getId());
                return null;
            }
            
            log.debug("Cliente encontrado para transacción {}: {} - {}", 
                    transaccion.getId(), cliente.getCedula(), cliente.getNombre());
            
            return cliente;
            
        } catch (Exception e) {
            log.error("Error al buscar cliente para transacción {}: {}", 
                     transaccion.getId(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Clase para estadísticas del servicio de alertas
     */
    public static class EstadisticasAlertas {
        private final int alertasEnCache;
        private final boolean saldoBajoHabilitado;
        private final boolean transaccionesProgramadasHabilitado;
        private final double umbralSaldoBajo;
        
        public EstadisticasAlertas(int cache, boolean saldoBajo, boolean transacciones, double umbral) {
            this.alertasEnCache = cache;
            this.saldoBajoHabilitado = saldoBajo;
            this.transaccionesProgramadasHabilitado = transacciones;
            this.umbralSaldoBajo = umbral;
        }
        
        // Getters
        public int getAlertasEnCache() { return alertasEnCache; }
        public boolean isSaldoBajoHabilitado() { return saldoBajoHabilitado; }
        public boolean isTransaccionesProgramadasHabilitado() { return transaccionesProgramadasHabilitado; }
        public double getUmbralSaldoBajo() { return umbralSaldoBajo; }
        
        @Override
        public String toString() {
            return String.format("EstadisticasAlertas{cache=%d, saldoBajo=%s, transacciones=%s, umbral=%.2f}", 
                               alertasEnCache, saldoBajoHabilitado, transaccionesProgramadasHabilitado, umbralSaldoBajo);
        }
    }
} 