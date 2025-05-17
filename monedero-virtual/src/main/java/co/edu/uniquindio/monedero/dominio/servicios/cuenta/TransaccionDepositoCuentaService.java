package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionDeposito;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.servicios.puntos.GestorPuntosService;
import co.edu.uniquindio.monedero.dominio.modelo.TipoTransaccion;
import org.springframework.stereotype.Service;

@Service
public class TransaccionDepositoCuentaService {

    private final CuentaDao cuentaDao;
    private final GestorPuntosService gestorPuntosService;

    public TransaccionDepositoCuentaService(CuentaDao cuentaDao, GestorPuntosService gestorPuntosService) {
        this.cuentaDao = cuentaDao;
        this.gestorPuntosService = gestorPuntosService;
    }

    public void procesarDeposito(String numeroCliente, TransaccionDeposito transaccionDeposito) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(numeroCliente);
        if (cuenta == null) {
            throw new IllegalArgumentException("No existe la cuenta con el número: " + numeroCliente);
        }
        if (transaccionDeposito == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula");
        }

        // Registrar la transacción y actualizar saldos
        cuenta.getTransacciones().insertarAlInicio(transaccionDeposito);
        cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() + transaccionDeposito.getMonto());
        cuenta.setSaldoTotal(cuenta.getSaldoTotal() + transaccionDeposito.getMonto());

        // Calcular y actualizar puntos
        int puntosGanados = gestorPuntosService.calcularYAgregarPuntos(
            numeroCliente,
            transaccionDeposito.getMonto(),
            TipoTransaccion.DEPOSITO
        );
        
        transaccionDeposito.setNumeroDePuntos(puntosGanados);
    }
}