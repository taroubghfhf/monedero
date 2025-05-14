package co.edu.uniquindio.monedero.infraestructura.adaptador.cuenta;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import org.springframework.stereotype.Component;

@Component
public class CuentaImplementacionDAO implements CuentaDao {


    @Override
    public Cuenta buscarPorNumeroCuenta(String numeroCuenta) {
        return null;
    }
}
