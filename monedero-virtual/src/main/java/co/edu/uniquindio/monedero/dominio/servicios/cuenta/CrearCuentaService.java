package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio.CuentaRepositorio;
import org.springframework.stereotype.Service;

@Service
public class CrearCuentaService {

    private final CuentaRepositorio cuentaRepositorio;
    private final ClienteDao clienteDao;
    private final CuentaDao cuentaDao;


    public CrearCuentaService(CuentaRepositorio cuentaRepositorio, ClienteDao clienteDao, CuentaDao cuentaDao) {
        this.cuentaRepositorio = cuentaRepositorio;
        this.clienteDao = clienteDao;
        this.cuentaDao = cuentaDao;
    }

    public boolean crearCuenta(Cuenta cuenta, String cedulaCliente) {
        if (clienteDao.buscarPorCedula(cedulaCliente) == null) {
            throw new IllegalArgumentException("El cliente no existe con la cédula: " + cedulaCliente);
        }
        if (cuentaDao.buscarPorNumeroCuenta(cuenta.getNumeroCuenta()) != null) {
            throw new IllegalArgumentException("Ya existe una cuenta con el número: " + cuenta.getNumeroCuenta());
        }
        return cuentaRepositorio.agregarCuenta(cuenta, cedulaCliente);
    }
}
