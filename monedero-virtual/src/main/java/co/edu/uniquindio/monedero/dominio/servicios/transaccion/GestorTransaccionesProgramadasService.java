package co.edu.uniquindio.monedero.dominio.servicios.transaccion;

import co.edu.uniquindio.monedero.dominio.modelo.*;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.puerto.transaccion.TransaccionProgramadaRepositorio;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestorTransaccionesProgramadasService {
    private final TransaccionProgramadaRepositorio repositorio;
    private final TransaccionDepositoCuentaService depositoService;
    private final TransaccionRetiroCuentaService retiroService;
    private final TransferenciaService transferenciaService;
    private final CuentaDao cuentaDao;

    public void programarTransaccion(TransaccionProgramada transaccion) {
        validarTransaccionProgramada(transaccion);
        repositorio.guardar(transaccion);
    }

    private void validarTransaccionProgramada(TransaccionProgramada transaccion) {
        if (transaccion.getFechaEjecucion().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de ejecución no puede ser anterior a la fecha actual");
        }

        if (transaccion.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        Cuenta cuentaOrigen = cuentaDao.buscarPorNumeroCuenta(transaccion.getClienteOrigen());
        if (cuentaOrigen == null) {
            throw new IllegalArgumentException("No existe una cuenta para el cliente origen: " + transaccion.getClienteOrigen());
        }

        switch (transaccion.getTipoTransaccion()) {
            case TRANSFERENCIA_SALIENTE:
                validarTransferencia(transaccion);
                break;
            case RETIRO:
            case DEPOSITO:
                validarClienteOrigen(transaccion);
                break;
            default:
                throw new IllegalArgumentException("Tipo de transacción no válido para programación");
        }
    }

    private void validarTransferencia(TransaccionProgramada transaccion) {
        if (transaccion.getClienteDestino() == null || 
            transaccion.getClienteDestino().trim().isEmpty()) {
            throw new IllegalArgumentException("Para transferencias se requiere un cliente destino");
        }
        
        Cuenta cuentaDestino = cuentaDao.buscarPorNumeroCuenta(transaccion.getClienteDestino());
        if (cuentaDestino == null) {
            throw new IllegalArgumentException("No existe una cuenta para el cliente destino: " + transaccion.getClienteDestino());
        }
        
        validarClienteOrigen(transaccion);
    }

    private void validarClienteOrigen(TransaccionProgramada transaccion) {
        if (transaccion.getClienteOrigen() == null || 
            transaccion.getClienteOrigen().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cliente origen es requerido");
        }
    }

    public void cancelarTransaccionPorId(String id) {
        TransaccionProgramada transaccion = repositorio.buscarPorId(id);
        if (transaccion == null) {
            throw new IllegalArgumentException("No existe una transacción programada con el ID: " + id);
        }
        repositorio.eliminar(id);
    }

    public List<TransaccionProgramada> listarTransaccionesPorCliente(String numeroCliente) {
        return repositorio.buscarPorNumeroCliente(numeroCliente);
    }

    @Scheduled(fixedRate = 60000)
    public void procesarTransaccionesPendientes() {
        List<TransaccionProgramada> transaccionesPendientes = repositorio.obtenerTransaccionesActivas();
        
        for (TransaccionProgramada transaccion : transaccionesPendientes) {
            try {
                ejecutarTransaccion(transaccion);
                if (transaccion.getPeriodicidad() != null) {
                    programarSiguienteEjecucion(transaccion);
                }
                repositorio.eliminar(transaccion.getId());
            } catch (Exception e) {
                System.err.println("Error al procesar transacción: " + e.getMessage());
            }
        }
    }

    private void ejecutarTransaccion(TransaccionProgramada transaccion) {
        switch (transaccion.getTipoTransaccion()) {
            case DEPOSITO:
                ejecutarDeposito(transaccion);
                break;
            case RETIRO:
                ejecutarRetiro(transaccion);
                break;
            case TRANSFERENCIA_SALIENTE:
                ejecutarTransferencia(transaccion);
                break;
        }
    }

    private void ejecutarDeposito(TransaccionProgramada transaccion) {
        TransaccionDeposito deposito = new TransaccionDeposito(transaccion.getMonto());
        depositoService.procesarDeposito(transaccion.getClienteOrigen(), deposito);
    }

    private void ejecutarRetiro(TransaccionProgramada transaccion) {
        TransaccionRetiro retiro = new TransaccionRetiro(transaccion.getMonto());
        retiroService.ejecutar(transaccion.getClienteOrigen(), retiro);
    }

    private void ejecutarTransferencia(TransaccionProgramada transaccion) {
        
        transferenciaService.ejecutar(
            transaccion.getClienteOrigen(),
            transaccion.getClienteDestino(),
            transaccion.getMonto()
        );
    }

    private void programarSiguienteEjecucion(TransaccionProgramada transaccion) {
        LocalDateTime nuevaFecha = calcularSiguienteFechaEjecucion(
            transaccion.getFechaEjecucion(), 
            transaccion.getPeriodicidad()
        );
        
        TransaccionProgramada nuevaTransaccion = new TransaccionProgramada(
            transaccion.getClienteOrigen(),
            transaccion.getClienteDestino(),
            transaccion.getMonto(),
            transaccion.getTipoTransaccion(),
            nuevaFecha,
            transaccion.getPeriodicidad()
        );
        
        repositorio.guardar(nuevaTransaccion);
    }

    private LocalDateTime calcularSiguienteFechaEjecucion(LocalDateTime fechaActual, Periodicidad periodicidad) {
        return switch (periodicidad) {
            case DIARIA -> fechaActual.plusDays(1);
            case SEMANAL -> fechaActual.plusWeeks(1);
            case QUINCENAL -> fechaActual.plusDays(15);
            case MENSUAL -> fechaActual.plusMonths(1);
        };
    }
} 