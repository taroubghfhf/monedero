package co.edu.uniquindio.monedero.aplicacion.manejador.cuenta;

import co.edu.uniquindio.monedero.dominio.dto.cuenta.CuentaDTO;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.BuscarCuentaService;
import org.springframework.stereotype.Component;

@Component
public class ManejadorConsultaCuenta {

    private final CuentaDao cuentaDao;

    public ManejadorConsultaCuenta(CuentaDao cuentaDao) {
        this.cuentaDao = cuentaDao;
    }

    public CuentaDTO ejecutar( String documento) {
        BuscarCuentaService buscarCuentaService = new BuscarCuentaService(cuentaDao);
        return buscarCuentaService.buscarCuenta(documento);
    }

}
