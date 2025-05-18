package co.edu.uniquindio.monedero.infraestructura.controlador.cliente;

import co.edu.uniquindio.monedero.aplicacion.comando.ClienteComando;
import co.edu.uniquindio.monedero.aplicacion.manejador.cliente.ManejadorConsultaCliente;
import co.edu.uniquindio.monedero.aplicacion.manejador.cliente.ManejadorCrearCliente;
import co.edu.uniquindio.monedero.aplicacion.manejador.cliente.ManejadorListarClientes;
import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ControladorComandoCliente {

    private final ManejadorCrearCliente manejadorCrearCliente;
    private final ManejadorConsultaCliente manejadorConsultaCliente;
    private final ManejadorListarClientes manejadorListarClientes;

    public ControladorComandoCliente(ManejadorCrearCliente manejadorCrearCliente,
                                      ManejadorConsultaCliente manejadorConsultaCliente,
                                      ManejadorListarClientes manejadorListarClientes) {
        this.manejadorCrearCliente = manejadorCrearCliente;
        this.manejadorConsultaCliente = manejadorConsultaCliente;
        this.manejadorListarClientes = manejadorListarClientes;
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

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarClientes() {
        List<ClienteDTO> clientes = manejadorListarClientes.ejecutar();
        return new ResponseEntity<>(clientes, HttpStatus.OK);
    }
}
