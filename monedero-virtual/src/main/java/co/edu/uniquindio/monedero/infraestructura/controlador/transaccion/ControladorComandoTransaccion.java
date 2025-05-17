package co.edu.uniquindio.monedero.infraestructura.controlador.transaccion;


import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionDepositoComando;
import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionRetiroComando;
import co.edu.uniquindio.monedero.aplicacion.comando.TransferenciaComando;
import co.edu.uniquindio.monedero.aplicacion.manejador.transaccion.ManejadorConsultarTransacciones;
import co.edu.uniquindio.monedero.aplicacion.manejador.transaccion.ManejadorTransaccionDeposito;
import co.edu.uniquindio.monedero.aplicacion.manejador.transaccion.ManejadorTransaccionRetiro;
import co.edu.uniquindio.monedero.aplicacion.manejador.transaccion.ManejadorTransferencia;
import co.edu.uniquindio.monedero.dominio.dto.cuenta.CuentaDTO;
import co.edu.uniquindio.monedero.dominio.dto.transaccion.TransaccionDTO;
import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimple;
import co.edu.uniquindio.monedero.dominio.dto.transferencia.ResultadoTransferenciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaccion")
public class ControladorComandoTransaccion {


    private final ManejadorTransaccionDeposito manejadorTransaccionDeposito;
    private final ManejadorTransaccionRetiro manejadorTransaccionRetiro;
    private final ManejadorConsultarTransacciones manejadorConsultarTransacciones;
    private final ManejadorTransferencia manejadorTransferencia;

    public ControladorComandoTransaccion(ManejadorTransaccionDeposito manejadorTransaccionDeposito,
                                         ManejadorTransaccionRetiro manejadorTransaccionRetiro,
                                         ManejadorConsultarTransacciones manejadorConsultarTransacciones,
                                         ManejadorTransferencia manejadorTransferencia) {
        this.manejadorTransaccionDeposito = manejadorTransaccionDeposito;
        this.manejadorTransaccionRetiro = manejadorTransaccionRetiro;
        this.manejadorConsultarTransacciones = manejadorConsultarTransacciones;
        this.manejadorTransferencia = manejadorTransferencia;
    }

    @PostMapping("/deposito")
    public ResponseEntity<Void> deposito(@RequestBody TransaccionDepositoComando comando) {
        manejadorTransaccionDeposito.ejecutar(comando);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/retiro")
    public ResponseEntity<Void> retiro(@RequestBody TransaccionRetiroComando comando) {
        manejadorTransaccionRetiro.ejecutar(comando);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @GetMapping("/{cedulaCliente}")
    public ResponseEntity<ListaSimple<TransaccionDTO>> retiro(@PathVariable("cedulaCliente") String cedulaCliente) {
        ListaSimple<TransaccionDTO> transacciones = manejadorConsultarTransacciones.ejecutar(cedulaCliente);
        return new ResponseEntity<>(transacciones, HttpStatus.OK);
    }

    @PostMapping("/transferencia")
    public ResponseEntity<ResultadoTransferenciaDTO> transferencia(@RequestBody TransferenciaComando comando) {
        ResultadoTransferenciaDTO resultado = manejadorTransferencia.ejecutar(comando);
        
        if (resultado.isExitoso()) {
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(resultado, HttpStatus.BAD_REQUEST);
        }
    }
}
