package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.exception.CuentaNoExisteException;
import co.edu.uniquindio.monedero.dominio.exception.YaExisteCuentaException;
import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio.CuentaRepositorio;
import org.springframework.stereotype.Service;

import java.util.Random;

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

    public boolean crearCuenta(String cedulaCliente) {
        String numeroCuenta = generarNumeroCuenta();

        if (clienteDao.buscarPorCedula(cedulaCliente) == null) {
            throw new CuentaNoExisteException("El cliente no existe con la cédula: " + cedulaCliente);
        }

        if (cuentaDao.existeCuenta(cedulaCliente)) {
            throw new YaExisteCuentaException("Ya existe una cuenta con el cliente: " + cedulaCliente);
        }

        Cuenta cuenta = new Cuenta(numeroCuenta);

        return cuentaRepositorio.agregarCuenta(cuenta, cedulaCliente);
    }


    private String generarNumeroCuenta() {
        Random random = new Random();
        StringBuilder numero = new StringBuilder();

        numero.append(random.nextInt(9) + 1);

        for (int i = 1; i < 15; i++) {
            numero.append(random.nextInt(10));
        }

        return numero.toString();
    }
}
