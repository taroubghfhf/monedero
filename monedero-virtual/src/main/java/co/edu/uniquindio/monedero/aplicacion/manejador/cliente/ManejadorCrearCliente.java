package co.edu.uniquindio.monedero.aplicacion.manejador.cliente;

import co.edu.uniquindio.monedero.aplicacion.comando.ClienteComando;
import co.edu.uniquindio.monedero.aplicacion.fabrica.cliente.FabricaCliente;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.repositorio.ClienteRepositorio;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.CrearClienteService;
import org.springframework.stereotype.Component;

@Component
public class ManejadorCrearCliente {

    private final ClienteDao clienteDao;
    private final ClienteRepositorio clienteRepositorio;

    public ManejadorCrearCliente(ClienteDao clienteDao,
                                 ClienteRepositorio clienteRepositorio) {
        this.clienteDao = clienteDao;
        this.clienteRepositorio = clienteRepositorio;
    }


    public void ejecutar(ClienteComando clienteComando) {
        Cliente cliente = FabricaCliente.crearCliente(clienteComando);
        CrearClienteService crearClienteService = new CrearClienteService(clienteDao, clienteRepositorio);

        crearClienteService.crearCliente(cliente);
    }
}