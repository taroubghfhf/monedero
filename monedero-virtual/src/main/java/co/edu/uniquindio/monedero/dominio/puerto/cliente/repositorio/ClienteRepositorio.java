package co.edu.uniquindio.monedero.dominio.puerto.cliente.repositorio;

import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import org.springframework.stereotype.Component;

@Component
public interface ClienteRepositorio {
    boolean agregarCliente(Cliente cliente);
}
