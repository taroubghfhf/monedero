package co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;


public interface CuentaRepositorio {
    boolean agregarCuenta(Cuenta cuenta, String cedulaCliente);
}