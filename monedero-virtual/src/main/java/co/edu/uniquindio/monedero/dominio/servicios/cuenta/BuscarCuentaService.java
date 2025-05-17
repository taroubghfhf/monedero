package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.dto.cuenta.CuentaDTO;
import co.edu.uniquindio.monedero.dominio.exception.CuentaNoExisteException;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import org.springframework.stereotype.Service;

@Service
public class BuscarCuentaService {

    private final CuentaDao cuentaDao;

    public BuscarCuentaService(CuentaDao CuentaDao) {
        this.cuentaDao = CuentaDao;
    }

    public CuentaDTO buscarCuenta(String cedulaCliente) {
        CuentaDTO cuentaDTO = cuentaDao.buscarPorNumeroCuentaNumeroCedula(cedulaCliente);
        if (cuentaDTO == null) {
            throw new CuentaNoExisteException("No existe una cuenta con el número: " + cedulaCliente);
        }

        return cuentaDTO;
    }
}
