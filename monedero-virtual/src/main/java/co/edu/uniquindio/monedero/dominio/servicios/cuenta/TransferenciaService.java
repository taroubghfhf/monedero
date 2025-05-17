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

    public TransferenciaService(CuentaDao cuentaDao, GestorPuntosService gestorPuntosService) {
        this.cuentaDao = cuentaDao;
        this.gestorPuntosService = gestorPuntosService;
    }

    public void ejecutar(String cedulaClienteOrigen, String cedulaClienteDestino, double monto) {
        validarDatosTransferencia(cedulaClienteOrigen, cedulaClienteDestino, monto);

        Cuenta cuentaOrigen = obtenerYValidarCuenta(cedulaClienteOrigen, "origen");
        Cuenta cuentaDestino = obtenerYValidarCuenta(cedulaClienteDestino, "destino");

        validarSaldoSuficiente(cuentaOrigen, monto);
        validarTransferenciaPropia(cedulaClienteOrigen, cedulaClienteDestino);

        RangoCliente rangoOrigen = gestorPuntosService.consultarRango(cedulaClienteOrigen);

        LocalDateTime fechaTransaccion = LocalDateTime.now();
        String idTransaccion = UUID.randomUUID().toString();

        realizarTransferencia(cuentaOrigen, cuentaDestino, monto, fechaTransaccion, idTransaccion);
        procesarPuntosTransferencia(cedulaClienteOrigen, monto);
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

    private void validarSaldoSuficiente(Cuenta cuenta, double monto) {
        if (cuenta.getSaldoCuenta() < monto) {
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente. Saldo actual: %.2f, Monto requerido: %.2f",
                    cuenta.getSaldoCuenta(), monto)
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
            LocalDateTime fechaTransaccion,
            String idTransaccion) {

        // Crear transacción saliente
        Transaccion transaccionSaliente = new Transaccion(
            idTransaccion,
            monto,
            fechaTransaccion,
            TipoTransaccion.TRANSFERENCIA_SALIENTE
        );

        // Crear transacción entrante
        Transaccion transaccionEntrante = new Transaccion(
            idTransaccion,
            monto,
            fechaTransaccion,
            TipoTransaccion.TRANSFERENCIA_ENTRANTE
        );

        // Actualizar cuenta origen
        actualizarCuentaOrigen(cuentaOrigen, transaccionSaliente, monto);

        // Actualizar cuenta destino
        actualizarCuentaDestino(cuentaDestino, transaccionEntrante, monto);
    }

    private void actualizarCuentaOrigen(Cuenta cuenta, Transaccion transaccion, double monto) {
        cuenta.getTransacciones().insertarAlInicio(transaccion);
        cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() - monto);
        cuenta.setSaldoTotal(cuenta.getSaldoTotal() - monto);
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