package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.exception.CuentaNoExisteException;
import co.edu.uniquindio.monedero.dominio.exception.SaldoInsuficienteException;
import co.edu.uniquindio.monedero.dominio.exception.TransaccionInvalidaException;
import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionRetiro;
import co.edu.uniquindio.monedero.dominio.modelo.Transaccion;
import co.edu.uniquindio.monedero.dominio.modelo.TipoTransaccion;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.servicios.puntos.GestorPuntosService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransaccionRetiroCuentaService {

    private static final double PORCENTAJE_CARGO = 0.10; // 10% de cargo
    private final CuentaDao cuentaDao;
    private final GestorPuntosService gestorPuntosService;

    public TransaccionRetiroCuentaService(CuentaDao cuentaDao, GestorPuntosService gestorPuntosService) {
        this.cuentaDao = cuentaDao;
        this.gestorPuntosService = gestorPuntosService;
    }

    public void ejecutar(String numeroCliente, TransaccionRetiro transaccionRetiro) {
        validarDatosRetiro(numeroCliente, transaccionRetiro);
        
        Cuenta cuenta = obtenerCuenta(numeroCliente);
        double montoRetiro = transaccionRetiro.getMonto();
        
        // Determinar si aplica cargo basado en el beneficio
        double montoCargo = 0.0;
        if (!gestorPuntosService.tieneExencionRetiros(numeroCliente)) {
            montoCargo = calcularCargo(montoRetiro);
        }
        
        double montoTotal = montoRetiro + montoCargo;
        validarSaldoSuficiente(cuenta, montoTotal);
        
        realizarRetiro(cuenta, transaccionRetiro, montoCargo);
        procesarPuntos(cuenta, transaccionRetiro);
    }

    private double calcularCargo(double montoRetiro) {
        return montoRetiro * PORCENTAJE_CARGO;
    }

    private void validarDatosRetiro(String numeroCliente, TransaccionRetiro transaccionRetiro) {
        if (numeroCliente == null || numeroCliente.trim().isEmpty()) {
            throw new TransaccionInvalidaException("El número de cliente es requerido");
        }
        if (transaccionRetiro == null) {
            throw new TransaccionInvalidaException("La transacción de retiro no puede ser nula");
        }
        if (transaccionRetiro.getMonto() <= 0) {
            throw new TransaccionInvalidaException("El monto del retiro debe ser mayor a cero");
        }
    }

    private Cuenta obtenerCuenta(String numeroCliente) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(numeroCliente);
        if (cuenta == null) {
            throw new CuentaNoExisteException("No existe la cuenta con el número: " + numeroCliente);
        }
        return cuenta;
    }

    private void validarSaldoSuficiente(Cuenta cuenta, double montoTotal) {
        if (cuenta.getSaldoCuenta() < montoTotal) {
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente. Saldo actual: %.2f, Monto requerido (incluyendo cargo): %.2f", 
                    cuenta.getSaldoCuenta(), montoTotal)
            );
        }
    }

    private void realizarRetiro(Cuenta cuenta, TransaccionRetiro transaccionRetiro, double montoCargo) {
        // Registrar la transacción de retiro
        cuenta.getTransacciones().insertarAlInicio(transaccionRetiro);

        // Si hay cargo (no tiene beneficio de exención), registrar la transacción de cargo
        if (montoCargo > 0) {
            Transaccion transaccionCargo = new Transaccion(montoCargo, TipoTransaccion.CARGO);
            cuenta.getTransacciones().insertarAlInicio(transaccionCargo);
        }
        
        // Actualizar saldos con el monto total (retiro + cargo si aplica)
        double montoTotal = transaccionRetiro.getMonto() + montoCargo;
        cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() - montoTotal);
        cuenta.setSaldoTotal(cuenta.getSaldoTotal() - montoTotal);
    }

    private void procesarPuntos(Cuenta cuenta, TransaccionRetiro transaccionRetiro) {
        int puntosGanados = gestorPuntosService.calcularYAgregarPuntos(
            cuenta.getNumeroCuenta(),
            transaccionRetiro.getMonto(),
            transaccionRetiro.getTipoTransaccion()
        );
        transaccionRetiro.setNumeroDePuntos(puntosGanados);
    }
}
