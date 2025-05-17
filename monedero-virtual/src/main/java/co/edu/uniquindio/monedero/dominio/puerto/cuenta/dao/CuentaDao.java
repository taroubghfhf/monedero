package co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao;

import co.edu.uniquindio.monedero.dominio.dto.cuenta.CuentaDTO;
import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;

public interface CuentaDao {

    CuentaDTO buscarPorNumeroCuentaNumeroCedula(String cedulaCliente);

    boolean existeCuenta(String cedulaCliente);

    Cuenta buscarPorNumeroCuenta(String cedulaCliente);

}