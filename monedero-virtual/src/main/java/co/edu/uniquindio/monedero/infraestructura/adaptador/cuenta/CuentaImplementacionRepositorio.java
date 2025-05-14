package co.edu.uniquindio.monedero.infraestructura.adaptador.cuenta;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio.CuentaRepositorio;
import org.springframework.stereotype.Repository;

@Repository
public class CuentaImplementacionRepositorio implements CuentaRepositorio {
    @Override
    public boolean agregarCuenta(Cuenta cuenta, String cedulaCliente) {
        return false;
    }
}
