package co.edu.uniquindio.monedero.aplicacion.manejador.cliente;

import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.BuscarClienteService;
import org.springframework.stereotype.Component;

@Component
public class ManejadorConsultaCliente {

    private final ClienteDao clienteDao;

    public ManejadorConsultaCliente(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public  ClienteDTO ejecutar(String documento) {
        BuscarClienteService buscarClienteService = new BuscarClienteService(clienteDao);
        return buscarClienteService.buscarPorCedula(documento);
    }
}
