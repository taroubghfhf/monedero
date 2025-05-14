package co.edu.uniquindio.monedero.infraestructura.controlador.cliente;

import co.edu.uniquindio.monedero.aplicacion.comando.ClienteComando;
import co.edu.uniquindio.monedero.aplicacion.manejador.cliente.ManejadorCrearCliente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente")
public class ControladorComandoCliente {

    private final ManejadorCrearCliente manejadorCrearCliente;

    public ControladorComandoCliente(ManejadorCrearCliente manejadorCrearCliente) {
        this.manejadorCrearCliente = manejadorCrearCliente;
    }

    @PostMapping
    public ResponseEntity<Void> crearCliente(@RequestBody ClienteComando comando) {
        manejadorCrearCliente.ejecutar(comando);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
