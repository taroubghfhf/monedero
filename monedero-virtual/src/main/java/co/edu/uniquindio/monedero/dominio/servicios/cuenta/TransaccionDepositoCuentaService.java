package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionDeposito;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import org.springframework.stereotype.Service;


@Service
public class TransaccionDepositoCuentaService {

    private final CuentaDao cuentaDao;

    public TransaccionDepositoCuentaService(CuentaDao cuentaDao) {
        this.cuentaDao = cuentaDao;
    }

    public void procesarDeposito(String numeroCuenta, TransaccionDeposito transaccionDeposito) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(numeroCuenta);
        if (cuenta == null) {
            throw new IllegalArgumentException("No existe la cuenta con el número: " + numeroCuenta);
        }
        if (transaccionDeposito == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula");
        }

        cuenta.getTransacciones().insertarAlInicio(transaccionDeposito);
        cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() + transaccionDeposito.getMonto());
        cuenta.setSaldoTotal(cuenta.getSaldoTotal() + transaccionDeposito.getMonto());
    }
}