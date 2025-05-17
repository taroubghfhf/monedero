package co.edu.uniquindio.monedero.infraestructura.controlador.cliente;

import co.edu.uniquindio.monedero.aplicacion.comando.ClienteComando;
import co.edu.uniquindio.monedero.aplicacion.manejador.cliente.ManejadorConsultaCliente;
import co.edu.uniquindio.monedero.aplicacion.manejador.cliente.ManejadorCrearCliente;
import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente")
public class ControladorComandoCliente {

    private final ManejadorCrearCliente manejadorCrearCliente;
    private final ManejadorConsultaCliente manejadorConsultaCliente;

    public ControladorComandoCliente(ManejadorCrearCliente manejadorCrearCliente,
                                      ManejadorConsultaCliente manejadorConsultaCliente) {
        this.manejadorCrearCliente = manejadorCrearCliente;
        this.manejadorConsultaCliente = manejadorConsultaCliente;
    }

    @PostMapping
    public ResponseEntity<Void> crearCliente(@RequestBody ClienteComando comando) {
        manejadorCrearCliente.ejecutar(comando);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @GetMapping("/document/{document}")
    public ResponseEntity<ClienteDTO> ejecutar(@PathVariable("document") String document) {
        ClienteDTO clienteDTO = manejadorConsultaCliente.ejecutar(document);
        return new ResponseEntity<>(clienteDTO, HttpStatus.OK);
    }
}
