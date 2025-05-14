package co.edu.uniquindio.monedero.dominio.servicios.cliente;

import co.edu.uniquindio.monedero.dominio.exception.ClienteYaExisteException;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.repositorio.ClienteRepositorio;
import org.springframework.stereotype.Service;

@Service
public class CrearClienteService {

    private final ClienteDao clienteDao;
    private final ClienteRepositorio clienteRepositorio;

    public CrearClienteService(ClienteDao clienteDao, ClienteRepositorio clienteRepositorio) {
        this.clienteDao = clienteDao;
        this.clienteRepositorio = clienteRepositorio;
    }

    public boolean crearCliente(Cliente cliente) {
        if (clienteDao.buscarPorCedula(cliente.getCedula()) != null) {
            throw new ClienteYaExisteException("El cliente ya existe con la cédula: " + cliente.getCedula());
        }
        return clienteRepositorio.agregarCliente(cliente);
    }
}
