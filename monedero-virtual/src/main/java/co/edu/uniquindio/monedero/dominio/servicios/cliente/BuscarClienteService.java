package co.edu.uniquindio.monedero.dominio.servicios.cliente;

import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import org.springframework.stereotype.Service;

@Service
public class BuscarClienteService {

    private final ClienteDao clienteDao;

    public BuscarClienteService(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public ClienteDTO buscarPorCedula(String cedula) {
        ClienteDTO cliente = clienteDao.buscarPorCedula(cedula);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no existe con la cédula: " + cedula);
        }
        return cliente;
    }
}