package co.edu.uniquindio.monedero.infraestructura.controlador.cuenta;

import co.edu.uniquindio.monedero.aplicacion.comando.CuentaComando;
import co.edu.uniquindio.monedero.aplicacion.manejador.cuenta.ManejadorConsultaCuenta;
import co.edu.uniquindio.monedero.aplicacion.manejador.cuenta.ManejadorCrearCuenta;
import co.edu.uniquindio.monedero.dominio.dto.cuenta.CuentaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuenta")
public class ControladorComandoCuenta {

    private final ManejadorCrearCuenta manejadorCrearCuenta;
    private final ManejadorConsultaCuenta manejadorConsultaCuenta;

    public ControladorComandoCuenta(ManejadorCrearCuenta manejadorCrearCuenta,
                                    ManejadorConsultaCuenta manejadorConsultaCuenta) {
        this.manejadorCrearCuenta = manejadorCrearCuenta;
        this.manejadorConsultaCuenta = manejadorConsultaCuenta;
    }

    @PostMapping
    public ResponseEntity<Void> crearCuenta(@RequestBody CuentaComando comando) {
        manejadorCrearCuenta.ejecutar(comando);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/buscar/{cedulaCliente}")
    public ResponseEntity<CuentaDTO> buscarCuenta(@PathVariable("cedulaCliente") String cedulaCliente) {
        CuentaDTO cuentaDTO = manejadorConsultaCuenta.ejecutar(cedulaCliente);
        return new ResponseEntity<>(cuentaDTO, HttpStatus.OK);
    }
}
