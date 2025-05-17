package co.edu.uniquindio.monedero.aplicacion.manejador.cuenta;

import co.edu.uniquindio.monedero.aplicacion.comando.CuentaComando;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio.CuentaRepositorio;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.CrearCuentaService;
import org.springframework.stereotype.Component;

@Component
public class ManejadorCrearCuenta {

    private final CuentaRepositorio cuentaRepositorio;
    private final ClienteDao clienteDao;
    private final CuentaDao cuentaDao;

    public ManejadorCrearCuenta(CuentaRepositorio cuentaRepositorio,
                                 CuentaDao cuentaDao,
                                 ClienteDao clienteDao) {
        this.cuentaRepositorio = cuentaRepositorio;
        this.cuentaDao = cuentaDao;
        this.clienteDao = clienteDao;
    }


    public void ejecutar(CuentaComando cuentaComando) {

        CrearCuentaService crearCuentaService = new CrearCuentaService(cuentaRepositorio, clienteDao, cuentaDao);

        crearCuentaService.crearCuenta(cuentaComando.getCedulaCliente());
    }
}
