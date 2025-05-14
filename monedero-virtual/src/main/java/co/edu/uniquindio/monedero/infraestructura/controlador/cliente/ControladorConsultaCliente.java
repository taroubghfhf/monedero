package co.edu.uniquindio.monedero.infraestructura.controlador.cliente;

import co.edu.uniquindio.monedero.aplicacion.manejador.cliente.ManejadorConsultaCliente;
import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente-consulta")
public class ControladorConsultaCliente {

    private final ManejadorConsultaCliente manejadorConsultaCliente;

    public ControladorConsultaCliente(ManejadorConsultaCliente manejadorConsultaCliente) {
        this.manejadorConsultaCliente = manejadorConsultaCliente;
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<ClienteDTO> ejecutar(@PathVariable String documento) {
        ClienteDTO clienteDTO = manejadorConsultaCliente.ejecutar(documento);
        return new ResponseEntity<>(clienteDTO, HttpStatus.OK);
    }
}
