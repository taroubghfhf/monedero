package co.edu.uniquindio.monedero.aplicacion.manejador.cliente;

import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.ListarClientesService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManejadorListarClientes {

    private final ClienteDao clienteDao;

    public ManejadorListarClientes(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public List<ClienteDTO> ejecutar() {
        ListarClientesService listarClientesService = new ListarClientesService(clienteDao);
        return listarClientesService.listarClientes();
    }
} 