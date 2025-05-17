package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.dominio.dto.transaccion.TransaccionDTO;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.ConsultarTransaccionesCuentaService;
import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimple;
import org.springframework.stereotype.Component;

@Component
public class ManejadorConsultarTransacciones {

    private final CuentaDao cuentaDao;

    public ManejadorConsultarTransacciones(CuentaDao cuentaDao) {
        this.cuentaDao = cuentaDao;
    }

    public ListaSimple<TransaccionDTO> ejecutar(String cedulaCliente) {
        ConsultarTransaccionesCuentaService consultarTransaccionesCuentaService =
                new ConsultarTransaccionesCuentaService(cuentaDao);

        return consultarTransaccionesCuentaService.ejecutar(cedulaCliente);
    }
}
