package co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;

public interface CuentaDao {
    Cuenta buscarPorNumeroCuenta(String numeroCuenta);
}