package co.edu.uniquindio.monedero.infraestructura.adaptador.cliente;

import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.repositorio.ClienteRepositorio;
import co.edu.uniquindio.monedero.infraestructura.persistencia.Lista;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteImplementacionRepositorio implements ClienteRepositorio {

    Lista listaClientes;

    public ClienteImplementacionRepositorio() {
        this.listaClientes = Lista.obtenerLista();
    }

    @Override
    public boolean agregarCliente(Cliente cliente) {
        listaClientes.agregarCliente(cliente);
        return true;
    }
}
