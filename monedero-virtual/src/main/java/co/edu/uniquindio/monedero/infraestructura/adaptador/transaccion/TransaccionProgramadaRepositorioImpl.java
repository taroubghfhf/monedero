package co.edu.uniquindio.monedero.infraestructura.adaptador.transaccion;

import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import co.edu.uniquindio.monedero.dominio.puerto.transaccion.TransaccionProgramadaRepositorio;
import co.edu.uniquindio.monedero.infraestructura.cola.ColaPrioridadTransacciones;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransaccionProgramadaRepositorioImpl implements TransaccionProgramadaRepositorio {
    private final ColaPrioridadTransacciones colaPrioridad;

    @Override
    public void guardar(TransaccionProgramada transaccion) {
        colaPrioridad.agregarTransaccion(transaccion);
    }

    @Override
    public void eliminar(String id) {
        TransaccionProgramada transaccion = buscarPorId(id);
        if (transaccion != null) {
            colaPrioridad.eliminarTransaccion(transaccion);
        }
    }

    @Override
    public TransaccionProgramada buscarPorId(String id) {
        var actual = colaPrioridad.getFrente();
        while (actual != null) {
            if (actual.getTransaccion().getId().equals(id)) {
                return actual.getTransaccion();
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    @Override
    public List<TransaccionProgramada> buscarPorNumeroCliente(String numeroCliente) {
        List<TransaccionProgramada> transacciones = new ArrayList<>();
        var actual = colaPrioridad.getFrente();
        
        while (actual != null) {
            TransaccionProgramada transaccion = actual.getTransaccion();
            if (transaccion.getClienteOrigen().equals(numeroCliente) ||
                transaccion.getClienteDestino().equals(numeroCliente)) {
                transacciones.add(transaccion);
            }
            actual = actual.getSiguiente();
        }
        return transacciones;
    }

    @Override
    public List<TransaccionProgramada> obtenerTransaccionesActivas() {
        List<TransaccionProgramada> activas = new ArrayList<>();
        var actual = colaPrioridad.getFrente();
        LocalDateTime ahora = LocalDateTime.now();
        
        while (actual != null) {
            TransaccionProgramada transaccion = actual.getTransaccion();
            if (transaccion.isActiva() && !transaccion.getFechaEjecucion().isAfter(ahora)) {
                activas.add(transaccion);
            }
            actual = actual.getSiguiente();
        }
        return activas;
    }

    @Override
    public List<TransaccionProgramada> buscarPorFechaEjecucionEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<TransaccionProgramada> transaccionesEnRango = new ArrayList<>();
        var actual = colaPrioridad.getFrente();
        
        while (actual != null) {
            TransaccionProgramada transaccion = actual.getTransaccion();
            LocalDateTime fechaEjecucion = transaccion.getFechaEjecucion();
            
            // Verificar que la transacción esté activa y dentro del rango de fechas
            if (transaccion.isActiva() && 
                fechaEjecucion.isAfter(fechaInicio) && 
                fechaEjecucion.isBefore(fechaFin)) {
                transaccionesEnRango.add(transaccion);
            }
            actual = actual.getSiguiente();
        }
        return transaccionesEnRango;
    }
} 