package co.edu.uniquindio.monedero.infraestructura.adaptador.cliente;

import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.infraestructura.persistencia.Lista;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClienteImplementacionDAO implements ClienteDao {

    Lista listaClientes;

    public ClienteImplementacionDAO() {
        this.listaClientes = Lista.obtenerLista();
    }

    @Override
    public ClienteDTO buscarPorCedula(String cedula) {
        Cliente cliente =  listaClientes.buscarCliente(cedula);

        if (cliente == null) {
            return null;
        }

        return new ClienteDTO(cliente.getNombre(), cliente.getApellido(), cliente.getCedula(),
                cliente.getCorreo(), cliente.getTelefono());
    }

    @Override
    public List<ClienteDTO> listarClientes() {
        List<ClienteDTO> clientesDTO = new ArrayList<>();
        List<Cliente> clientes = listaClientes.obtenerTodosLosClientes();
        
        for (Cliente cliente : clientes) {
            clientesDTO.add(new ClienteDTO(
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getCedula(),
                cliente.getCorreo(),
                cliente.getTelefono()
            ));
        }
        
        return clientesDTO;
    }
}
