package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionRetiroComando;
import co.edu.uniquindio.monedero.aplicacion.fabrica.transaccion.FabricaTransaccion;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionRetiro;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.TransaccionRetiroCuentaService;
import co.edu.uniquindio.monedero.dominio.servicios.puntos.GestorPuntosService;
import org.springframework.stereotype.Component;

@Component
public class ManejadorTransaccionRetiro {

    private final CuentaDao cuentaDao;
    private final GestorPuntosService gestorPuntosService;

    public ManejadorTransaccionRetiro(CuentaDao cuentaDao, GestorPuntosService gestorPuntosService) {
        this.cuentaDao = cuentaDao;
        this.gestorPuntosService = gestorPuntosService;
    }

    public void ejecutar(TransaccionRetiroComando comando) {
        TransaccionRetiroCuentaService transaccionRetiroCuentaService = 
            new TransaccionRetiroCuentaService(cuentaDao, gestorPuntosService);
            
        TransaccionRetiro transaccionRetiro = FabricaTransaccion.crear(comando);
        
        transaccionRetiroCuentaService.ejecutar(
            comando.getCedulaCliente(), 
            transaccionRetiro
        );
    }
}
