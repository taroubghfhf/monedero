package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionDepositoComando;
import co.edu.uniquindio.monedero.aplicacion.fabrica.transaccion.FabricaTransaccion;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionDeposito;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.TransaccionDepositoCuentaService;
import co.edu.uniquindio.monedero.dominio.servicios.puntos.GestorPuntosService;
import org.springframework.stereotype.Component;

@Component
public class ManejadorTransaccionDeposito {

    private final CuentaDao cuentaDao;
    private final GestorPuntosService gestorPuntosService;

    public ManejadorTransaccionDeposito(CuentaDao cuentaDao, GestorPuntosService gestorPuntosService) {
        this.cuentaDao = cuentaDao;
        this.gestorPuntosService = gestorPuntosService;
    }

    public void ejecutar(TransaccionDepositoComando comando) {
        TransaccionDepositoCuentaService transaccionDepositoCuentaService = 
            new TransaccionDepositoCuentaService(cuentaDao, gestorPuntosService);
            
        TransaccionDeposito transaccionDeposito = FabricaTransaccion.crear(comando);
        
        transaccionDepositoCuentaService.procesarDeposito(
            comando.getCedulaCliente(), 
            transaccionDeposito
        );
    }
}
