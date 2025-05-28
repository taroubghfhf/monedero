package co.edu.uniquindio.monedero.infraestructura.notificaciones;

import co.edu.uniquindio.monedero.dominio.dto.NotificacionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementación de una lista circular para gestionar notificaciones de manera eficiente
 * Utiliza un buffer circular con control de concurrencia para optimizar el manejo de notificaciones
 */
@Component
@Slf4j
public class ListaCircularNotificaciones {
    
    private final NotificacionDTO[] buffer;
    private final AtomicInteger writeIndex;
    private final AtomicInteger readIndex;
    private final AtomicInteger size;
    private final int capacity;
    private final ReentrantReadWriteLock lock;
    
    public ListaCircularNotificaciones() {
        this.capacity = 100; // Tamaño por defecto, puede ser configurable
        this.buffer = new NotificacionDTO[capacity];
        this.writeIndex = new AtomicInteger(0);
        this.readIndex = new AtomicInteger(0);
        this.size = new AtomicInteger(0);
        this.lock = new ReentrantReadWriteLock();
    }
    
    /**
     * Agrega una notificación a la lista circular
     * Si la lista está llena, sobrescribe la notificación más antigua
     */
    public boolean agregar(NotificacionDTO notificacion) {
        lock.writeLock().lock();
        try {
            int currentWriteIndex = writeIndex.get();
            buffer[currentWriteIndex] = notificacion;
            
            // Avanzar el índice de escritura circularmente
            writeIndex.set((currentWriteIndex + 1) % capacity);
            
            // Si la lista está llena, avanzar también el índice de lectura
            if (size.get() == capacity) {
                readIndex.set((readIndex.get() + 1) % capacity);
                log.warn("Lista circular llena. Sobrescribiendo notificación antigua: {}", 
                        buffer[currentWriteIndex] != null ? buffer[currentWriteIndex].getId() : "null");
            } else {
                size.incrementAndGet();
            }
            
            log.debug("Notificación agregada: {} - Tamaño actual: {}", notificacion.getId(), size.get());
            return true;
            
        } catch (Exception e) {
            log.error("Error al agregar notificación: {}", e.getMessage(), e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtiene la siguiente notificación para procesar
     */
    public NotificacionDTO obtenerSiguiente() {
        lock.readLock().lock();
        try {
            if (size.get() == 0) {
                return null;
            }
            
            int currentReadIndex = readIndex.get();
            NotificacionDTO notificacion = buffer[currentReadIndex];
            
            if (notificacion != null) {
                log.debug("Obteniendo notificación: {} - Posición: {}", notificacion.getId(), currentReadIndex);
            }
            
            return notificacion;
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Marca una notificación como procesada y la remueve de la lista
     */
    public boolean marcarComoProcesada(String notificacionId) {
        lock.writeLock().lock();
        try {
            if (size.get() == 0) {
                return false;
            }
            
            int currentReadIndex = readIndex.get();
            NotificacionDTO notificacion = buffer[currentReadIndex];
            
            if (notificacion != null && notificacion.getId().equals(notificacionId)) {
                buffer[currentReadIndex] = null;
                readIndex.set((currentReadIndex + 1) % capacity);
                size.decrementAndGet();
                
                log.debug("Notificación marcada como procesada: {} - Tamaño actual: {}", 
                         notificacionId, size.get());
                return true;
            }
            
            return false;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtiene todas las notificaciones pendientes por prioridad
     */
    public List<NotificacionDTO> obtenerNotificacionesPorPrioridad(NotificacionDTO.PrioridadNotificacion prioridad) {
        lock.readLock().lock();
        try {
            List<NotificacionDTO> notificacionesFiltradas = new ArrayList<>();
            
            for (int i = 0; i < size.get(); i++) {
                int index = (readIndex.get() + i) % capacity;
                NotificacionDTO notificacion = buffer[index];
                
                if (notificacion != null && notificacion.getPrioridad() == prioridad) {
                    notificacionesFiltradas.add(notificacion);
                }
            }
            
            log.debug("Encontradas {} notificaciones con prioridad: {}", 
                     notificacionesFiltradas.size(), prioridad);
            
            return notificacionesFiltradas;
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Obtiene el número actual de notificaciones en la lista
     */
    public int getTamaño() {
        return size.get();
    }
    
    /**
     * Verifica si la lista está vacía
     */
    public boolean estaVacia() {
        return size.get() == 0;
    }
    
    /**
     * Verifica si la lista está llena
     */
    public boolean estaLlena() {
        return size.get() == capacity;
    }
    
    /**
     * Limpia todas las notificaciones de la lista
     */
    public void limpiar() {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < capacity; i++) {
                buffer[i] = null;
            }
            writeIndex.set(0);
            readIndex.set(0);
            size.set(0);
            
            log.info("Lista circular de notificaciones limpiada");
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtiene estadísticas de la lista circular
     */
    public EstadisticasListaCircular obtenerEstadisticas() {
        lock.readLock().lock();
        try {
            return new EstadisticasListaCircular(
                size.get(),
                capacity,
                (double) size.get() / capacity * 100,
                readIndex.get(),
                writeIndex.get()
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Clase para estadísticas de la lista circular
     */
    public static class EstadisticasListaCircular {
        private final int tamaño;
        private final int capacidad;
        private final double porcentajeUso;
        private final int indiceLeft;
        private final int indiceEscritura;
        
        public EstadisticasListaCircular(int tamaño, int capacidad, double porcentajeUso, 
                                       int indiceLectura, int indiceEscritura) {
            this.tamaño = tamaño;
            this.capacidad = capacidad;
            this.porcentajeUso = porcentajeUso;
            this.indiceLeft = indiceLectura;
            this.indiceEscritura = indiceEscritura;
        }
        
        // Getters
        public int getTamaño() { return tamaño; }
        public int getCapacidad() { return capacidad; }
        public double getPorcentajeUso() { return porcentajeUso; }
        public int getIndiceLectura() { return indiceLeft; }
        public int getIndiceEscritura() { return indiceEscritura; }
        
        @Override
        public String toString() {
            return String.format("EstadisticasListaCircular{tamaño=%d, capacidad=%d, uso=%.2f%%, " +
                               "lectura=%d, escritura=%d}", 
                               tamaño, capacidad, porcentajeUso, indiceLeft, indiceEscritura);
        }
    }
} 