package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.exception.CuentaNoExisteException;
import co.edu.uniquindio.monedero.dominio.exception.SaldoInsuficienteException;
import co.edu.uniquindio.monedero.dominio.exception.TransferenciaInvalidaException;
import co.edu.uniquindio.monedero.dominio.modelo.*;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.servicios.puntos.GestorPuntosService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferenciaService {

    private final CuentaDao cuentaDao;
    private final GestorPuntosService gestorPuntosService;
    private static final double PORCENTAJE_COMISION = 0.20; // 20% de comisión

    public TransferenciaService(CuentaDao cuentaDao, GestorPuntosService gestorPuntosService) {
        this.cuentaDao = cuentaDao;
        this.gestorPuntosService = gestorPuntosService;
    }

    public void ejecutar(String cedulaClienteOrigen, String cedulaClienteDestino, double monto) {
        validarDatosTransferencia(cedulaClienteOrigen, cedulaClienteDestino, monto);

        Cuenta cuentaOrigen = obtenerYValidarCuenta(cedulaClienteOrigen, "origen");
        Cuenta cuentaDestino = obtenerYValidarCuenta(cedulaClienteDestino, "destino");

        // Calcular comisión con beneficio de reducción si aplica
        double comisionBase = monto * PORCENTAJE_COMISION;
        double comision = aplicarDescuentoComision(cedulaClienteOrigen, comisionBase);
        double montoTotal = monto + comision;

        validarSaldoSuficiente(cuentaOrigen, montoTotal);
        validarTransferenciaPropia(cedulaClienteOrigen, cedulaClienteDestino);

        RangoCliente rangoOrigen = gestorPuntosService.consultarRango(cedulaClienteOrigen);

        LocalDateTime fechaTransaccion = LocalDateTime.now();
        String idTransaccion = UUID.randomUUID().toString();

        realizarTransferencia(cuentaOrigen, cuentaDestino, monto, comision, fechaTransaccion, idTransaccion);
        procesarPuntosTransferencia(cedulaClienteOrigen, monto);
    }
    
    private double aplicarDescuentoComision(String cedulaCliente, double comisionBase) {
        // Verificar si el cliente tiene el beneficio de reducción de comisión activo
        double descuento = gestorPuntosService.obtenerDescuentoTransferencia(cedulaCliente);
        
        if (descuento > 0) {
            // Aplicar el descuento a la comisión base
            return comisionBase * (1 - descuento);
        }
        
        return comisionBase;
    }

    private void validarDatosTransferencia(String cedulaOrigen, String cedulaDestino, double monto) {
        if (monto <= 0) {
            throw new TransferenciaInvalidaException("El monto de la transferencia debe ser mayor a cero");
        }
        if (cedulaOrigen == null || cedulaOrigen.trim().isEmpty()) {
            throw new TransferenciaInvalidaException("La cédula del cliente origen es requerida");
        }
        if (cedulaDestino == null || cedulaDestino.trim().isEmpty()) {
            throw new TransferenciaInvalidaException("La cédula del cliente destino es requerida");
        }
    }

    private Cuenta obtenerYValidarCuenta(String cedula, String tipo) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedula);
        if (cuenta == null) {
            throw new CuentaNoExisteException(
                String.format("No existe la cuenta %s con el número: %s", tipo, cedula)
            );
        }
        return cuenta;
    }

    private void validarSaldoSuficiente(Cuenta cuenta, double montoTotal) {
        if (cuenta.getSaldoCuenta() < montoTotal) {
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente. Saldo actual: %.2f, Monto requerido (con comisión): %.2f",
                    cuenta.getSaldoCuenta(), montoTotal)
            );
        }
    }

    private void validarTransferenciaPropia(String cedulaOrigen, String cedulaDestino) {
        if (cedulaOrigen.equals(cedulaDestino)) {
            throw new TransferenciaInvalidaException(
                "No se puede realizar una transferencia a la misma cuenta"
            );
        }
    }

    private void realizarTransferencia(
            Cuenta cuentaOrigen,
            Cuenta cuentaDestino,
            double monto,
            double comision,
            LocalDateTime fechaTransaccion,
            String idTransaccion) {

        // Crear transacción saliente (usando TransaccionTransferencia para incluir comisión)
        TransaccionTransferencia transaccionSaliente = new TransaccionTransferencia(monto);
        transaccionSaliente.setId(idTransaccion);
        transaccionSaliente.setFecha(fechaTransaccion);
        transaccionSaliente.setComision(comision);

        // Crear transacción entrante
        Transaccion transaccionEntrante = new Transaccion(
            idTransaccion,
            monto,
            fechaTransaccion,
            TipoTransaccion.TRANSFERENCIA_ENTRANTE
        );
        
        // Crear transacción para el cargo de comisión
        String idCargo = UUID.randomUUID().toString();
        Transaccion transaccionCargo = new Transaccion(
            idCargo,
            comision,
            fechaTransaccion,
            TipoTransaccion.CARGO
        );

        // Actualizar cuenta origen - ahora descontando monto + comisión
        actualizarCuentaOrigen(cuentaOrigen, transaccionSaliente, transaccionCargo, monto, comision);

        // Actualizar cuenta destino
        actualizarCuentaDestino(cuentaDestino, transaccionEntrante, monto);
    }

    private void actualizarCuentaOrigen(
            Cuenta cuenta, 
            TransaccionTransferencia transaccionTransferencia, 
            Transaccion transaccionCargo,
            double monto, 
            double comision) {
        // Registrar transacción de transferencia
        cuenta.getTransacciones().insertarAlInicio(transaccionTransferencia);
        // Registrar transacción de cargo por comisión
        cuenta.getTransacciones().insertarAlInicio(transaccionCargo);
        
        double montoTotal = monto + comision;
        cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() - montoTotal);
        cuenta.setSaldoTotal(cuenta.getSaldoTotal() - montoTotal);
    }

    private void actualizarCuentaDestino(Cuenta cuenta, Transaccion transaccion, double monto) {
        cuenta.getTransacciones().insertarAlInicio(transaccion);
        cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() + monto);
        cuenta.setSaldoTotal(cuenta.getSaldoTotal() + monto);
    }

    private void procesarPuntosTransferencia(String cedulaCliente, double monto) {
        int puntosGanados = gestorPuntosService.calcularYAgregarPuntos(
            cedulaCliente,
            monto,
            TipoTransaccion.TRANSFERENCIA_SALIENTE
        );

        Cuenta cuentaOrigen = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuentaOrigen != null) {
            cuentaOrigen.setTotalPuntos(cuentaOrigen.getTotalPuntos() + puntosGanados);
        }
    }
} 