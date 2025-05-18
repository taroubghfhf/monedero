package co.edu.uniquindio.monedero.dominio.puerto.cliente.dao;

import co.edu.uniquindio.monedero.dominio.dto.cliente.ClienteDTO;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ClienteDao {
    ClienteDTO buscarPorCedula(String cedula);
    List<ClienteDTO> listarClientes();
}
