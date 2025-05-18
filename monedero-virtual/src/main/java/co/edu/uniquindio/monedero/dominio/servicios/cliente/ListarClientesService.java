package co.edu.uniquindio.monedero.dominio.servicios.cliente;

import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarClientesService {

    private final ClienteDao clienteDao;

    public ListarClientesService(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public List<ClienteDTO> listarClientes() {
        return clienteDao.listarClientes();
    }
} 